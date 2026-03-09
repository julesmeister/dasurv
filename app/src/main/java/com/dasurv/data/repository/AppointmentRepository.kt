package com.dasurv.data.repository

import com.dasurv.data.local.dao.AppointmentDao
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.RecurrenceType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    fun getAllAppointments(): Flow<List<Appointment>> = appointmentDao.getAllAppointments()

    suspend fun getAppointmentById(id: Long): Appointment? = appointmentDao.getAppointmentById(id)

    fun getAppointmentsForClient(clientId: Long): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsForClient(clientId)

    fun getAppointmentsInRange(startTime: Long, endTime: Long): Flow<List<Appointment>> =
        appointmentDao.getAppointmentsInRange(startTime, endTime)

    fun getUpcomingAppointments(now: Long, limit: Int = 5): Flow<List<Appointment>> =
        appointmentDao.getUpcomingAppointments(now, limit)

    suspend fun getScheduledAppointmentsWithReminder(): List<Appointment> =
        appointmentDao.getScheduledAppointmentsWithReminder()

    suspend fun insertAppointment(appointment: Appointment): Long =
        appointmentDao.insertAppointment(appointment)

    suspend fun updateAppointment(appointment: Appointment) =
        appointmentDao.updateAppointment(appointment)

    suspend fun deleteAppointment(appointment: Appointment) =
        appointmentDao.deleteAppointment(appointment)

    fun getRecurringSeries(parentId: Long): Flow<List<Appointment>> =
        appointmentDao.getRecurringSeries(parentId)

    suspend fun deleteRecurringSeries(parentId: Long) =
        appointmentDao.deleteRecurringSeries(parentId)

    fun searchAppointments(query: String): Flow<List<Appointment>> =
        appointmentDao.searchAppointments(query)

    suspend fun getNextAppointment(now: Long): Appointment? =
        appointmentDao.getNextAppointment(now)

    suspend fun getLatestAppointment(): Appointment? =
        appointmentDao.getLatestAppointment()

    /**
     * Creates a recurring appointment series. Inserts the parent appointment first,
     * then generates child appointments at intervals until the end date.
     */
    suspend fun createRecurringSeries(appointment: Appointment): Long {
        // Insert parent
        val parentId = appointmentDao.insertAppointment(appointment)

        if (appointment.recurrenceType == RecurrenceType.NONE || appointment.recurrenceEndDate == null) {
            return parentId
        }

        val intervalDays = when (appointment.recurrenceType) {
            RecurrenceType.DAILY -> 1
            RecurrenceType.WEEKLY -> 7
            RecurrenceType.BIWEEKLY -> 14
            RecurrenceType.MONTHLY -> 30
            RecurrenceType.NONE -> return parentId
        }.let { if (appointment.recurrenceIntervalDays > 0) appointment.recurrenceIntervalDays else it }

        val children = mutableListOf<Appointment>()
        val cal = Calendar.getInstance().apply { timeInMillis = appointment.scheduledDateTime }

        while (true) {
            cal.add(Calendar.DAY_OF_YEAR, intervalDays)
            if (cal.timeInMillis > appointment.recurrenceEndDate) break
            children.add(
                appointment.copy(
                    id = 0,
                    scheduledDateTime = cal.timeInMillis,
                    parentAppointmentId = parentId
                )
            )
        }

        if (children.isNotEmpty()) {
            appointmentDao.insertAppointments(children)
        }

        return parentId
    }
}
