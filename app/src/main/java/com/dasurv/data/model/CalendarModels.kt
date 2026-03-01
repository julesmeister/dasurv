package com.dasurv.data.model

import androidx.compose.runtime.Immutable
import com.dasurv.data.local.entity.Appointment

@Immutable
data class CalendarDay(
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val appointments: List<Appointment> = emptyList()
)

@Immutable
data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>
)

@Immutable
data class AppointmentWithClient(
    val appointment: Appointment,
    val clientName: String
)
