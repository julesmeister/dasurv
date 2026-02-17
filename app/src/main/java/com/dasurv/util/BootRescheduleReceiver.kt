package com.dasurv.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dasurv.data.local.dao.AppointmentDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootRescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var appointmentDao: AppointmentDao
    @Inject lateinit var alarmScheduler: AppointmentAlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appointments = appointmentDao.getScheduledAppointmentsWithReminder()
                val now = System.currentTimeMillis()
                for (appointment in appointments) {
                    val triggerAt = appointment.scheduledDateTime -
                        appointment.reminderMinutesBefore * 60_000L
                    if (triggerAt > now) {
                        alarmScheduler.scheduleReminder(appointment.id, triggerAt)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
