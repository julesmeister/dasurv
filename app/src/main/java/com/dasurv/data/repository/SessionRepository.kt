package com.dasurv.data.repository

import com.dasurv.data.local.dao.SessionDao
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.SessionPigment
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
}
