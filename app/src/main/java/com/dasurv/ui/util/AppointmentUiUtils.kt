package com.dasurv.ui.util

import androidx.compose.ui.graphics.Color
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.ui.component.*

fun AppointmentStatus.statusColor(): Color = when (this) {
    AppointmentStatus.SCHEDULED -> M3Primary
    AppointmentStatus.COMPLETED -> M3GreenColor
    AppointmentStatus.CANCELLED -> M3OnSurfaceVariant
    AppointmentStatus.NO_SHOW -> M3RedColor
}

fun AppointmentStatus.statusContainerColor(): Color = when (this) {
    AppointmentStatus.SCHEDULED -> M3PrimaryContainer
    AppointmentStatus.COMPLETED -> M3GreenContainer
    AppointmentStatus.CANCELLED -> M3FieldBg
    AppointmentStatus.NO_SHOW -> M3RedContainer
}
