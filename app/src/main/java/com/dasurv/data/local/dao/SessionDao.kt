package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.SessionEquipment
import com.dasurv.data.local.entity.SessionPigment
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE clientId = :clientId ORDER BY date DESC")
    fun getSessionsForClient(clientId: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT COUNT(*) FROM sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Update
    suspend fun updateSession(session: Session)

    @Delete
    suspend fun deleteSession(session: Session)

    @Query("SELECT * FROM session_pigments WHERE sessionId = :sessionId")
    fun getPigmentsForSession(sessionId: Long): Flow<List<SessionPigment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionPigment(pigment: SessionPigment): Long

    @Delete
    suspend fun deleteSessionPigment(pigment: SessionPigment)

    // Session equipment
    @Query("SELECT * FROM session_equipment WHERE sessionId = :sessionId")
    fun getEquipmentForSession(sessionId: Long): Flow<List<SessionEquipment>>

    @Query("SELECT * FROM session_equipment WHERE sessionId = :sessionId")
    suspend fun getEquipmentForSessionOnce(sessionId: Long): List<SessionEquipment>

    @Insert
    suspend fun insertSessionEquipment(sessionEquipment: SessionEquipment): Long

    @Query("DELETE FROM session_equipment WHERE sessionId = :sessionId")
    suspend fun deleteSessionEquipmentBySession(sessionId: Long)
}
