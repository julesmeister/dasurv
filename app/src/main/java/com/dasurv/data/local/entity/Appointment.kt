package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AppointmentStatus {
    SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, BIWEEKLY, MONTHLY
}

@Stable
@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("clientId"), Index("sessionId"), Index("scheduledDateTime")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val scheduledDateTime: Long,
    val durationMinutes: Int = 60,
    val procedureType: String = "",
    val notes: String = "",
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val sessionId: Long? = null,
    val reminderEnabled: Boolean = true,
    val reminderMinutesBefore: Int = 30,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val recurrenceIntervalDays: Int = 0,
    val recurrenceEndDate: Long? = null,
    val parentAppointmentId: Long? = null,
    val staffId: Long? = null
)
