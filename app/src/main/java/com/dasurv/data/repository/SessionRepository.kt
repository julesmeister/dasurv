package com.dasurv.data.repository

import androidx.room.withTransaction
import com.dasurv.data.local.DasurvDatabase
import com.dasurv.data.local.dao.SessionDao
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.SessionPigment
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.screen.session.PigmentBottleSessionEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getSessionsForClient(clientId: Long): Flow<List<Session>> =
        sessionDao.getSessionsForClient(clientId)

    fun getAllSessions(): Flow<List<Session>> = sessionDao.getAllSessions()

    fun getSessionCount(): Flow<Int> = sessionDao.getSessionCount()

    suspend fun getSessionById(id: Long): Session? = sessionDao.getSessionById(id)

    suspend fun insertSession(session: Session): Long = sessionDao.insertSession(session)

    suspend fun updateSession(session: Session) = sessionDao.updateSession(session)

    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)

    fun getPigmentsForSession(sessionId: Long): Flow<List<SessionPigment>> =
        sessionDao.getPigmentsForSession(sessionId)

    suspend fun insertSessionPigment(pigment: SessionPigment): Long =
        sessionDao.insertSessionPigment(pigment)

    suspend fun deleteSessionPigment(pigment: SessionPigment) =
        sessionDao.deleteSessionPigment(pigment)

    // Session equipment
    fun getEquipmentForSession(sessionId: Long): Flow<List<SessionEquipment>> =
        sessionDao.getEquipmentForSession(sessionId)

    suspend fun getEquipmentForSessionOnce(sessionId: Long): List<SessionEquipment> =
        sessionDao.getEquipmentForSessionOnce(sessionId)

    suspend fun insertSessionEquipment(sessionEquipment: SessionEquipment): Long =
        sessionDao.insertSessionEquipment(sessionEquipment)

    suspend fun deleteSessionEquipmentBySession(sessionId: Long) =
        sessionDao.deleteSessionEquipmentBySession(sessionId)

    fun searchSessions(query: String): Flow<List<Session>> = sessionDao.searchSessions(query)

    suspend fun createSessionWithDependencies(
        database: DasurvDatabase,
        session: Session,
        selectedEquipmentIds: Set<Long>,
        equipmentQuantities: Map<Long, Double>,
        equipmentList: List<Equipment>,
        selectedBottleIds: Set<Long>,
        bottleEntries: Map<Long, PigmentBottleSessionEntry>,
        equipmentRepository: EquipmentRepository,
        pigmentBottleRepository: PigmentBottleRepository,
        transactionRepository: TransactionRepository
    ): Long = database.withTransaction {
        val sid = insertSession(session)

        // Persist session equipment
        val consumables = equipmentList.filter { it.id in selectedEquipmentIds && it.type == "consumable" }

        for (eq in consumables) {
            val qty = equipmentQuantities[eq.id] ?: 1.0
            insertSessionEquipment(
                SessionEquipment(
                    sessionId = sid,
                    equipmentId = eq.id,
                    quantityUsed = qty,
                    costPerPiece = eq.costPerPiece
                )
            )
        }
        // Batch deduct stock for all consumables
        val consumableIds = consumables.map { it.id }
        if (consumableIds.isNotEmpty()) {
            val currentEquipment = equipmentRepository.getEquipmentByIds(consumableIds)
            val updated = currentEquipment.map { eq ->
                val qty = equipmentQuantities[eq.id] ?: 1.0
                eq.copy(stockQuantity = (eq.stockQuantity - qty.toInt()).coerceAtLeast(0))
            }
            equipmentRepository.updateEquipmentBatch(updated)
        }

        // Persist pigment bottle usages
        val bottleIds = selectedBottleIds.toList()
        val bottlesMap = if (bottleIds.isNotEmpty()) {
            pigmentBottleRepository.getBottlesByIds(bottleIds).associateBy { it.id }
        } else emptyMap()

        val updatedBottles = mutableListOf<PigmentBottle>()
        for (bottleId in selectedBottleIds) {
            val entry = bottleEntries[bottleId] ?: continue
            val bottle = bottlesMap[bottleId] ?: continue
            val costAtUse = entry.mlUsed * bottle.pricePerMl

            pigmentBottleRepository.insertUsage(
                PigmentBottleUsage(
                    bottleId = bottleId,
                    clientId = session.clientId,
                    sessionId = sid,
                    lipArea = entry.lipArea,
                    mlUsed = entry.mlUsed,
                    costAtTimeOfUse = costAtUse
                )
            )
            updatedBottles.add(
                bottle.copy(remainingMl = (bottle.remainingMl - entry.mlUsed).coerceAtLeast(0.0))
            )
        }
        // Batch update all bottles
        if (updatedBottles.isNotEmpty()) {
            pigmentBottleRepository.updateBottleBatch(updatedBottles)
        }

        // Auto-create CHARGE transaction if session has cost
        if (session.totalCost > 0) {
            transactionRepository.insertTransaction(
                ClientTransaction(
                    clientId = session.clientId,
                    sessionId = sid,
                    type = TransactionType.CHARGE,
                    amount = session.totalCost,
                    date = session.date,
                    notes = "Session: ${session.procedure.ifBlank { "Session" }}"
                )
            )
        }

        sid
    }
}
