package com.dasurv.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.SessionPigment
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.data.model.CostItem
import com.dasurv.data.model.CostSummary
import com.dasurv.data.repository.EquipmentRepository
import com.dasurv.data.repository.PigmentBottleRepository
import com.dasurv.data.repository.SessionRepository
import com.dasurv.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedSession = MutableStateFlow<Session?>(null)
    val selectedSession: StateFlow<Session?> = _selectedSession

    val allEquipment = equipmentRepository.getAllEquipment()

    private val _sessionPigments = MutableStateFlow<List<SessionPigment>>(emptyList())
    val sessionPigments: StateFlow<List<SessionPigment>> = _sessionPigments

    private val _sessionEquipmentList = MutableStateFlow<List<SessionEquipment>>(emptyList())
    val sessionEquipmentList: StateFlow<List<SessionEquipment>> = _sessionEquipmentList

    // Map of equipmentId -> quantity used (for new session form)
    private val _equipmentQuantities = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val equipmentQuantities: StateFlow<Map<Long, Double>> = _equipmentQuantities

    private val _selectedEquipmentIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedEquipmentIds: StateFlow<Set<Long>> = _selectedEquipmentIds

    // Pigment bottle state
    val allBottles = pigmentBottleRepository.getInStockBottles()

    private val _selectedBottleIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedBottleIds: StateFlow<Set<Long>> = _selectedBottleIds

    private val _bottleEntries = MutableStateFlow<Map<Long, PigmentBottleSessionEntry>>(emptyMap())
    val bottleEntries: StateFlow<Map<Long, PigmentBottleSessionEntry>> = _bottleEntries

    private val _sessionBottleUsages = MutableStateFlow<List<PigmentBottleUsage>>(emptyList())
    val sessionBottleUsages: StateFlow<List<PigmentBottleUsage>> = _sessionBottleUsages

    fun loadSession(id: Long) {
        viewModelScope.launch {
            _selectedSession.value = sessionRepository.getSessionById(id)
            sessionRepository.getPigmentsForSession(id).collect {
                _sessionPigments.value = it
            }
        }
        viewModelScope.launch {
            sessionRepository.getEquipmentForSession(id).collect {
                _sessionEquipmentList.value = it
            }
        }
        viewModelScope.launch {
            pigmentBottleRepository.getUsageForSession(id).collect {
                _sessionBottleUsages.value = it
            }
        }
    }

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
                    "$${String.format("%.4f", eq.costPerPiece)}/pc from $${String.format("%.2f", eq.costPerUnit)} pkg"
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
                perPieceInfo = "$${String.format("%.4f", bottle.pricePerMl)}/ml"
            )
        }

        val allItems = equipmentItems + bottleItems
        return CostSummary(items = allItems, totalCost = allItems.sumOf { it.totalCost })
    }

    fun saveSession(session: Session, equipmentList: List<Equipment>, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val sessionId = sessionRepository.insertSession(session)

            // Persist session equipment
            val selectedIds = _selectedEquipmentIds.value
            val quantities = _equipmentQuantities.value
            val consumables = equipmentList.filter { it.id in selectedIds && it.type == "consumable" }

            for (eq in consumables) {
                val qty = quantities[eq.id] ?: 1.0
                sessionRepository.insertSessionEquipment(
                    SessionEquipment(
                        sessionId = sessionId,
                        equipmentId = eq.id,
                        quantityUsed = qty,
                        costPerPiece = eq.costPerPiece
                    )
                )
                // Deduct from stock
                val current = equipmentRepository.getEquipmentById(eq.id)
                if (current != null) {
                    equipmentRepository.updateEquipment(
                        current.copy(stockQuantity = (current.stockQuantity - qty.toInt()).coerceAtLeast(0))
                    )
                }
            }

            // Persist pigment bottle usages
            val selectedBottles = _selectedBottleIds.value
            val entries = _bottleEntries.value
            for (bottleId in selectedBottles) {
                val entry = entries[bottleId] ?: continue
                val bottle = pigmentBottleRepository.getBottleById(bottleId) ?: continue
                val costAtUse = entry.mlUsed * bottle.pricePerMl

                pigmentBottleRepository.insertUsage(
                    PigmentBottleUsage(
                        bottleId = bottleId,
                        clientId = session.clientId,
                        sessionId = sessionId,
                        lipArea = entry.lipArea,
                        mlUsed = entry.mlUsed,
                        costAtTimeOfUse = costAtUse
                    )
                )
                // Deduct remaining ml
                pigmentBottleRepository.updateBottle(
                    bottle.copy(remainingMl = (bottle.remainingMl - entry.mlUsed).coerceAtLeast(0.0))
                )
            }

            // Auto-create CHARGE transaction if session has cost
            if (session.totalCost > 0) {
                transactionRepository.insertTransaction(
                    ClientTransaction(
                        clientId = session.clientId,
                        sessionId = sessionId,
                        type = TransactionType.CHARGE,
                        amount = session.totalCost,
                        date = session.date,
                        notes = "Session: ${session.procedure.ifBlank { "Session" }}"
                    )
                )
            }

            onSuccess(sessionId)
        }
    }

    fun deleteSession(session: Session, onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
            onSuccess()
        }
    }
}
