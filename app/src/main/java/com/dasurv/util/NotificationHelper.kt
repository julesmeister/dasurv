package com.dasurv.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dasurv.MainActivity
import com.dasurv.R

object NotificationHelper {

    const val CHANNEL_ID = "appointment_reminders"
    private const val CHANNEL_NAME = "Appointment Reminders"
    private const val CHANNEL_DESCRIPTION = "Reminders for upcoming appointments"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildAppointmentNotification(
        context: Context,
        appointmentId: Long,
        clientName: String,
        procedureType: String,
        minutesBefore: Int
    ): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("appointmentId", appointmentId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, appointmentId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "Upcoming Appointment"
        val text = buildString {
            append("$clientName")
            if (procedureType.isNotBlank()) append(" - $procedureType")
            append(" in $minutesBefore minutes")
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }
}
