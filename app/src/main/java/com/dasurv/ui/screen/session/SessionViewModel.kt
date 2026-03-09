package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.data.model.CostItem
import com.dasurv.data.model.CostSummary
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.data.local.entity.SessionTemplateEquipment
import com.dasurv.data.local.entity.Staff
import com.dasurv.data.repository.EquipmentRepository
import com.dasurv.data.repository.PigmentBottleRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.SessionTemplateRepository
import com.dasurv.data.repository.StaffRepository
import com.dasurv.util.DefaultSubscribePolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatPrecise
import javax.inject.Inject

data class PigmentBottleSessionEntry(
    val mlUsed: Double = 0.5,
    val lipArea: UsageLipArea = UsageLipArea.BOTH
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val equipmentRepository: EquipmentRepository,
    private val pigmentBottleRepository: PigmentBottleRepository,
    private val sessionTemplateRepository: SessionTemplateRepository,
    private val staffRepository: StaffRepository
) : ViewModel() {

    val allEquipment = equipmentRepository.getAllEquipment()

    val activeStaff: StateFlow<List<Staff>> = staffRepository.getActiveStaff()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    // Map of equipmentId -> quantity used (for new session form)
    private val _equipmentQuantities = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val equipmentQuantities: StateFlow<Map<Long, Double>> = _equipmentQuantities

    private val _selectedEquipmentIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedEquipmentIds: StateFlow<Set<Long>> = _selectedEquipmentIds

    // Pigment bottle state
    val allBottles = pigmentBottleRepository.getInStockBottles()

    private val _selectedBottleIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedBottleIds: StateFlow<Set<Long>> = _selectedBottleIds

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage

    fun clearSnackbar() { _snackbarMessage.value = null }

    private val _bottleEntries = MutableStateFlow<Map<Long, PigmentBottleSessionEntry>>(emptyMap())
    val bottleEntries: StateFlow<Map<Long, PigmentBottleSessionEntry>> = _bottleEntries

    fun toggleEquipment(id: Long) {
        _selectedEquipmentIds.value = _selectedEquipmentIds.value.let {
            if (id in it) it - id else it + id
        }
        // Set default quantity of 1 when toggling on
        if (id in _selectedEquipmentIds.value && id !in _equipmentQuantities.value) {
            _equipmentQuantities.value = _equipmentQuantities.value + (id to 1.0)
        }
    }

    fun setEquipmentQuantity(equipmentId: Long, quantity: Double) {
        _equipmentQuantities.value = _equipmentQuantities.value + (equipmentId to quantity)
    }

    fun toggleBottle(id: Long) {
        _selectedBottleIds.value = _selectedBottleIds.value.let {
            if (id in it) it - id else it + id
        }
        if (id in _selectedBottleIds.value && id !in _bottleEntries.value) {
            _bottleEntries.value = _bottleEntries.value + (id to PigmentBottleSessionEntry())
        }
    }

    fun setBottleMlUsed(bottleId: Long, ml: Double) {
        val existing = _bottleEntries.value[bottleId] ?: PigmentBottleSessionEntry()
        _bottleEntries.value = _bottleEntries.value + (bottleId to existing.copy(mlUsed = ml))
    }

    fun setBottleLipArea(bottleId: Long, lipArea: UsageLipArea) {
        val existing = _bottleEntries.value[bottleId] ?: PigmentBottleSessionEntry()
        _bottleEntries.value = _bottleEntries.value + (bottleId to existing.copy(lipArea = lipArea))
    }

    fun calculateCost(equipmentList: List<Equipment>, bottleList: List<PigmentBottle> = emptyList()): CostSummary {
        val selectedIds = _selectedEquipmentIds.value
        val quantities = _equipmentQuantities.value
        val consumables = equipmentList.filter { it.id in selectedIds && it.type == "consumable" }
        val equipmentItems = consumables.map { eq ->
            val qty = quantities[eq.id] ?: 1.0
            CostItem(
                name = eq.name,
                category = eq.category,
                quantity = qty,
                unitCost = eq.costPerPiece,
                perPieceInfo = if (eq.piecesPerPackage > 1)
                    "₱${eq.costPerPiece.formatPrecise()}/pc from ₱${eq.costPerUnit.formatCurrency()} pkg"
                else ""
            )
        }

        val selectedBottles = _selectedBottleIds.value
        val entries = _bottleEntries.value
        val bottleItems = bottleList.filter { it.id in selectedBottles }.map { bottle ->
            val entry = entries[bottle.id] ?: PigmentBottleSessionEntry()
            CostItem(
                name = "${bottle.pigmentName} (${bottle.pigmentBrand})",
                category = "pigment",
                quantity = entry.mlUsed,
                unitCost = bottle.pricePerMl,
                perPieceInfo = "₱${bottle.pricePerMl.formatPrecise()}/ml"
            )
        }

        val allItems = equipmentItems + bottleItems
        return CostSummary(items = allItems, totalCost = allItems.sumOf { it.totalCost })
    }

    fun saveSession(session: Session, equipmentList: List<Equipment>, onSuccess: (Long) -> Unit, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val sessionId = sessionRepository.createSessionWithDependencies(
                    session = session,
                    selectedEquipmentIds = _selectedEquipmentIds.value,
                    equipmentQuantities = _equipmentQuantities.value,
                    equipmentList = equipmentList,
                    selectedBottleIds = _selectedBottleIds.value,
                    bottleEntries = _bottleEntries.value
                )
                onSuccess(sessionId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save session")
            }
        }
    }

    // Template support
    val allTemplates = sessionTemplateRepository.getAllTemplates()
        .stateIn(viewModelScope, DefaultSubscribePolicy, emptyList())

    fun loadTemplate(template: SessionTemplate, onLoaded: (String) -> Unit = {}) {
        viewModelScope.launch {
            val templateEquipment = sessionTemplateRepository.getEquipmentForTemplate(template.id)
            val equipmentIds = templateEquipment.map { it.equipmentId }.toSet()
            val quantities = templateEquipment.associate { it.equipmentId to it.quantity.toDouble() }
            _selectedEquipmentIds.value = equipmentIds
            _equipmentQuantities.value = quantities
            onLoaded(template.procedure)
        }
    }

    fun saveAsTemplate(name: String, procedure: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val template = SessionTemplate(name = name, procedure = procedure)
            val templateId = sessionTemplateRepository.insertTemplate(template)
            val selectedIds = _selectedEquipmentIds.value
            val quantities = _equipmentQuantities.value
            for (eqId in selectedIds) {
                val qty = quantities[eqId] ?: 1.0
                sessionTemplateRepository.insertTemplateEquipment(
                    SessionTemplateEquipment(
                        templateId = templateId,
                        equipmentId = eqId,
                        quantity = qty.toInt().coerceAtLeast(1)
                    )
                )
            }
            _snackbarMessage.value = "Saved as template"
            onSuccess()
        }
    }
}
