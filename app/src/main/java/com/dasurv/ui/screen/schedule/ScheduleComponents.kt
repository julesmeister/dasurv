package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.model.CalendarDay
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.ui.util.statusColor
import com.dasurv.ui.util.statusContainerColor
import com.dasurv.util.FMT_TIME
import com.dasurv.util.formatDurationMinutes
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> M3PrimaryContainer
        day.isToday -> M3PrimaryContainer.copy(alpha = 0.4f)
        else -> Color.Transparent
    }
    val textColor = when {
        !day.isCurrentMonth -> M3OnSurface.copy(alpha = 0.25f)
        isSelected -> M3Primary
        day.isToday -> M3Primary
        else -> M3OnSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (day.appointments.isNotEmpty() && day.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) M3Primary else M3AmberColor)
                )
            }
        }
    }
}

@Composable
internal fun AppointmentListRow(
    awc: AppointmentWithClient,
    onClick: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    val statusColor = awc.appointment.status.statusColor()
    val statusContainerColor = awc.appointment.status.statusContainerColor()

    val timeFormat = remember { SimpleDateFormat(FMT_TIME, Locale.getDefault()) }
    val durationText = formatDurationMinutes(awc.appointment.durationMinutes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(statusContainerColor)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeFormat.format(Date(awc.appointment.scheduledDateTime)),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.width(spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = awc.clientName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = M3OnSurface
            )
            Text(
                text = listOfNotNull(
                    awc.appointment.procedureType.ifBlank { null },
                    durationText,
                    awc.appointment.status.name.lowercase().replaceFirstChar { it.uppercase() }
                ).joinToString("  ·  "),
                fontSize = 12.sp,
                color = M3OnSurfaceVariant
            )
        }
    }
}
