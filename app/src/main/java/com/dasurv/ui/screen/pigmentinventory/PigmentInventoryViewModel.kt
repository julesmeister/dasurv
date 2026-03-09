package com.dasurv.ui.screen.pigmentinventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.data.repository.ClientRepository
import com.dasurv.data.repository.EquipmentRepository
import com.dasurv.util.DefaultSubscribePolicy
import com.dasurv.data.repository.PigmentBottleRepository
import com.dasurv.data.repository.PigmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PigmentInventoryViewModel @Inject constructor(
    private val pigmentBottleRepository: PigmentBottleRepository,
    private val pigmentRepository: PigmentRepository,
    private val clientRepository: ClientRepository,
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {

    val allBottles = pigmentBottleRepository.getAllBottles()
    val allClients = clientRepository.getAllClients()
    val allPigments = pigmentRepository.getAllPigments()
    val pigmentStock = equipmentRepository.getEquipmentByCategory("pigment")

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    private val _brandFilter = MutableStateFlow<String?>(null)
    val brandFilter: StateFlow<String?> = _brandFilter

    private val _selectedBottle = MutableStateFlow<PigmentBottle?>(null)
    val selectedBottle: StateFlow<PigmentBottle?> = _selectedBottle

    private val _selectedStock = MutableStateFlow<Equipment?>(null)
    val selectedStock: StateFlow<Equipment?> = _selectedStock

    private val _selectedBottleId = MutableStateFlow<Long?>(null)

    val usageHistory: StateFlow<List<PigmentBottleUsage>> = _selectedBottleId
        .filterNotNull()
        .flatMapLatest { pigmentBottleRepository.getUsageForBottle(it) }
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    fun setBrandFilter(brand: String?) {
        _brandFilter.value = brand
    }

    fun loadBottle(id: Long) {
        _selectedBottleId.value = id
        viewModelScope.launch {
            _selectedBottle.value = pigmentBottleRepository.getBottleById(id)
        }
    }

    fun loadStock(id: Long) {
        viewModelScope.launch {
            _selectedStock.value = equipmentRepository.getEquipmentById(id)
        }
    }

    fun saveBottle(bottle: PigmentBottle, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val isNew = bottle.id == 0L
            if (isNew) {
                pigmentBottleRepository.insertBottle(bottle)
            } else {
                pigmentBottleRepository.updateBottle(bottle)
            }
            _snackbarMessage.value = if (isNew) "Bottle added" else "Bottle updated"
            onSuccess()
        }
    }

    fun deleteBottle(bottle: PigmentBottle, onSuccess: () -> Unit) {
        viewModelScope.launch {
            pigmentBottleRepository.deleteBottle(bottle)
            _snackbarMessage.value = "Bottle deleted"
            onSuccess()
        }
    }

    // Stock management
    fun saveStock(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val isNew = equipment.id == 0L
            if (isNew) {
                equipmentRepository.insertEquipment(equipment)
            } else {
                equipmentRepository.updateEquipment(equipment)
            }
            _snackbarMessage.value = if (isNew) "Pigment stock added" else "Pigment stock updated"
            onSuccess()
        }
    }

    fun deleteStock(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.deleteEquipment(equipment)
            _snackbarMessage.value = "Pigment stock deleted"
            onSuccess()
        }
    }

    fun restock(equipment: Equipment, additionalCount: Int) {
        viewModelScope.launch {
            equipmentRepository.updateEquipment(
                equipment.copy(stockQuantity = equipment.stockQuantity + additionalCount)
            )
            _snackbarMessage.value = "Restocked +$additionalCount"
        }
    }

    fun openBottle(equipment: Equipment, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val catalogPigment = pigmentRepository.getPigmentByName(equipment.name)
            val colorHex = catalogPigment?.colorHex ?: "#CCCCCC"
            val brandDisplay = catalogPigment?.brand?.displayName ?: equipment.brand

            val bottleSizeMl = 15.0
            val pricePerBottle = equipment.costPerUnit
            val pricePerMl = if (bottleSizeMl > 0) pricePerBottle / bottleSizeMl else 0.0

            val bottleId = pigmentBottleRepository.insertBottle(
                PigmentBottle(
                    pigmentName = equipment.name,
                    pigmentBrand = brandDisplay,
                    colorHex = colorHex,
                    isCustom = catalogPigment == null,
                    bottleSizeMl = bottleSizeMl,
                    remainingMl = bottleSizeMl,
                    pricePerBottle = pricePerBottle,
                    pricePerMl = pricePerMl,
                    equipmentId = equipment.id
                )
            )

            // Deduct 1 from stock
            equipmentRepository.updateEquipment(
                equipment.copy(stockQuantity = (equipment.stockQuantity - 1).coerceAtLeast(0))
            )

            _snackbarMessage.value = "Bottle opened"
            onSuccess(bottleId)
        }
    }

    // Pigment usage logging
    fun logUsage(
        bottleId: Long,
        clientId: Long,
        lipArea: UsageLipArea,
        mlUsed: Double,
        sessionId: Long? = null,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val bottle = pigmentBottleRepository.getBottleById(bottleId) ?: return@launch
            val costAtUse = mlUsed * bottle.pricePerMl

            pigmentBottleRepository.insertUsage(
                PigmentBottleUsage(
                    bottleId = bottleId,
                    clientId = clientId,
                    sessionId = sessionId,
                    lipArea = lipArea,
                    mlUsed = mlUsed,
                    costAtTimeOfUse = costAtUse,
                    notes = notes
                )
            )

            // Deduct remaining ml
            pigmentBottleRepository.updateBottle(
                bottle.copy(remainingMl = (bottle.remainingMl - mlUsed).coerceAtLeast(0.0))
            )
            _snackbarMessage.value = "Usage logged"
        }
    }
}
