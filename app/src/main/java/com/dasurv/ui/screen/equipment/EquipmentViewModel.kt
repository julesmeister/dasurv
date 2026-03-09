package com.dasurv.ui.screen.equipment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentPurchase
import com.dasurv.data.local.entity.EquipmentUsage
import com.dasurv.data.repository.EquipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val equipmentRepository: EquipmentRepository
) : ViewModel() {

    val equipment = equipmentRepository.getAllEquipment()
    val purchaseSources = equipmentRepository.getDistinctPurchaseSources()
    val sellers = equipmentRepository.getDistinctSellers()

    private val _selectedEquipment = MutableStateFlow<Equipment?>(null)
    val selectedEquipment: StateFlow<Equipment?> = _selectedEquipment

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun clearSelectedEquipment() {
        _selectedEquipment.value = null
    }

    fun loadEquipment(id: Long) {
        viewModelScope.launch {
            _selectedEquipment.value = equipmentRepository.getEquipmentById(id)
        }
    }

    fun saveEquipment(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val isNew = equipment.id == 0L
            if (isNew) {
                equipmentRepository.insertEquipment(equipment)
            } else {
                equipmentRepository.updateEquipment(equipment)
            }
            _snackbarMessage.value = if (isNew) "Equipment added" else "Equipment updated"
            onSuccess()
        }
    }

    fun deleteEquipment(equipment: Equipment, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.deleteEquipment(equipment)
            _snackbarMessage.value = "Equipment deleted"
            onSuccess()
        }
    }

    fun logUsage(equipmentId: Long, quantity: Double, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.insertUsageAndDeductStock(
                EquipmentUsage(
                    equipmentId = equipmentId,
                    quantityUsed = quantity,
                    notes = notes
                )
            )
            _snackbarMessage.value = "Usage logged"
            onSuccess()
        }
    }

    // Purchase history
    private val _purchaseDateRange = MutableStateFlow<Pair<Long?, Long?>>(
        run {
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
            cal.set(java.util.Calendar.MILLISECOND, 999)
            val end = cal.timeInMillis
            cal.add(java.util.Calendar.MONTH, -1)
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            start to end
        }
    )
    val purchaseDateRange: StateFlow<Pair<Long?, Long?>> = _purchaseDateRange

    @OptIn(ExperimentalCoroutinesApi::class)
    val allPurchases = _purchaseDateRange.flatMapLatest { (start, end) ->
        if (start != null && end != null) {
            equipmentRepository.getPurchasesBetween(start, end)
        } else {
            equipmentRepository.getAllPurchases()
        }
    }

    fun setPurchaseDateRange(start: Long?, end: Long?) {
        _purchaseDateRange.value = start to end
    }

    fun recordPurchase(
        equipmentId: Long,
        quantity: Int,
        totalCost: Double,
        purchaseDate: Long,
        notes: String,
        purchaseSource: String = "",
        seller: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            equipmentRepository.insertPurchaseAndAddStock(
                EquipmentPurchase(
                    equipmentId = equipmentId,
                    quantity = quantity,
                    totalCost = totalCost,
                    purchaseDate = purchaseDate,
                    notes = notes,
                    purchaseSource = purchaseSource,
                    seller = seller
                )
            )
            _snackbarMessage.value = "Purchase recorded"
            onSuccess()
        }
    }

    fun deletePurchase(purchase: EquipmentPurchase, onSuccess: () -> Unit) {
        viewModelScope.launch {
            equipmentRepository.deletePurchase(purchase)
            _snackbarMessage.value = "Purchase deleted"
            onSuccess()
        }
    }
}
