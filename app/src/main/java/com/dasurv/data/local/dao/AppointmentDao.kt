package com.dasurv.data.local.dao

import androidx.room.*
import com.dasurv.data.local.entity.Appointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY scheduledDateTime ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY scheduledDateTime DESC")
    fun getAppointmentsForClient(clientId: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE scheduledDateTime BETWEEN :startTime AND :endTime ORDER BY scheduledDateTime ASC")
    fun getAppointmentsInRange(startTime: Long, endTime: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND scheduledDateTime >= :now ORDER BY scheduledDateTime ASC LIMIT :limit")
    fun getUpcomingAppointments(now: Long, limit: Int = 5): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE status = 'SCHEDULED' AND reminderEnabled = 1")
    suspend fun getScheduledAppointmentsWithReminder(): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Insert
    suspend fun insertAppointments(appointments: List<Appointment>): List<Long>

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE parentAppointmentId = :parentId ORDER BY scheduledDateTime ASC")
    fun getRecurringSeries(parentId: Long): Flow<List<Appointment>>

    @Query("DELETE FROM appointments WHERE parentAppointmentId = :parentId")
    suspend fun deleteRecurringSeries(parentId: Long)

    @Query("SELECT * FROM appointments WHERE procedureType LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    fun searchAppointments(query: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE scheduledDateTime >= :now ORDER BY scheduledDateTime ASC LIMIT 1")
    suspend fun getNextAppointment(now: Long): Appointment?

    @Query("SELECT * FROM appointments ORDER BY scheduledDateTime DESC LIMIT 1")
    suspend fun getLatestAppointment(): Appointment?
}
