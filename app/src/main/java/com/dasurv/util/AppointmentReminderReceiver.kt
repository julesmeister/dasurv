package com.dasurv.util

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dasurv.data.local.dao.AppointmentDao
import com.dasurv.data.local.dao.ClientDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppointmentReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var appointmentDao: AppointmentDao
    @Inject lateinit var clientDao: ClientDao

    companion object {
        const val EXTRA_APPOINTMENT_ID = "appointment_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getLongExtra(EXTRA_APPOINTMENT_ID, -1)
        if (appointmentId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appointment = appointmentDao.getAppointmentById(appointmentId) ?: return@launch
                val client = clientDao.getClientById(appointment.clientId) ?: return@launch

                val notification = NotificationHelper.buildAppointmentNotification(
                    context = context,
                    appointmentId = appointment.id,
                    clientName = client.name,
                    procedureType = appointment.procedureType,
                    minutesBefore = appointment.reminderMinutesBefore
                ).build()

                val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                if (hasPermission) {
                    NotificationManagerCompat.from(context)
                        .notify(appointmentId.toInt(), notification)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
