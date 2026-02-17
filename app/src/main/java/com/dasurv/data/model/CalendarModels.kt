package com.dasurv.data.model

import com.dasurv.data.local.entity.Appointment

data class CalendarDay(
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val appointments: List<Appointment> = emptyList()
)

data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>
)

data class AppointmentWithClient(
    val appointment: Appointment,
    val clientName: String
)
