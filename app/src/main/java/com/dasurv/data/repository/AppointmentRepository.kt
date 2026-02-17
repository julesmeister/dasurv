package com.dasurv.data.repository

import com.dasurv.data.local.dao.AppointmentDao
import com.dasurv.data.local.entity.Appointment
import kotlinx.coroutines.flow.Flow
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
}
