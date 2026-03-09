package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.data.local.entity.Staff
import com.dasurv.ui.component.*
import com.dasurv.ui.util.statusColor
import com.dasurv.ui.util.statusContainerColor
import com.dasurv.util.FMT_TIME
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun AppointmentInfoCard(
    appt: Appointment,
    clientName: String,
    activeStaff: List<Staff>,
    showStatusMenu: Boolean,
    onStatusMenuToggle: (Boolean) -> Unit,
    onStatusChange: (AppointmentStatus) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat(FMT_TIME, Locale.getDefault()) }
    val statusColor = appt.status.statusColor()
    val statusContainerColor = appt.status.statusContainerColor()

    M3ListCard {
        Column {
            // Client name + date header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        clientName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = M3OnSurface,
                    )
                    Text(
                        dateFormat.format(Date(appt.scheduledDateTime)),
                        fontSize = 13.sp,
                        color = M3OnSurfaceVariant,
                    )
                }
                // Status badge
                Box {
                    Surface(
                        onClick = { onStatusMenuToggle(true) },
                        shape = RoundedCornerShape(10.dp),
                        color = statusContainerColor,
                    ) {
                        Text(
                            appt.status.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                        )
                    }
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { onStatusMenuToggle(false) },
                        scrollState = rememberScrollState(),
                        shadowElevation = 0.dp,
                    ) {
                        AppointmentStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        status.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = M3OnSurface,
                                    )
                                },
                                onClick = {
                                    onStatusMenuToggle(false)
                                    onStatusChange(status)
                                }
                            )
                        }
                    }
                }
            }

            DetailDivider()

            // Time
            DetailValueRow(
                icon = Icons.Default.Schedule,
                label = "Time",
                value = timeFormat.format(Date(appt.scheduledDateTime)),
            )

            DetailDivider()

            // Duration
            DetailValueRow(
                icon = Icons.Default.AccessTime,
                label = "Duration",
                value = "${appt.durationMinutes} min",
                iconTint = M3AmberColor,
                iconBg = M3AmberColor.copy(alpha = 0.10f),
                valueBg = M3AmberColor.copy(alpha = 0.08f),
                valueColor = M3AmberColor,
            )

            if (appt.procedureType.isNotBlank()) {
                DetailDivider()
                DetailValueRow(
                    icon = Icons.Default.MedicalServices,
                    label = "Procedure",
                    value = appt.procedureType,
                )
            }

            if (appt.staffId != null) {
                val staffName = activeStaff.find { it.id == appt.staffId }?.name ?: "Unknown"
                DetailDivider()
                DetailValueRow(
                    icon = Icons.Default.Person,
                    label = "Staff",
                    value = staffName,
                    iconTint = M3PinkAccent,
                    iconBg = M3PinkAccent.copy(alpha = 0.10f),
                    valueBg = M3PinkAccent.copy(alpha = 0.08f),
                    valueColor = M3PinkAccent,
                )
            }

            if (appt.reminderEnabled) {
                DetailDivider()
                DetailValueRow(
                    icon = Icons.Default.Notifications,
                    label = "Reminder",
                    value = "${appt.reminderMinutesBefore} min before",
                    iconTint = M3AmberColor,
                    iconBg = M3AmberColor.copy(alpha = 0.10f),
                    valueBg = M3AmberColor.copy(alpha = 0.08f),
                    valueColor = M3AmberColor,
                )
            }

            if (appt.recurrenceType != com.dasurv.data.local.entity.RecurrenceType.NONE) {
                DetailDivider()
                DetailValueRow(
                    icon = Icons.Default.Repeat,
                    label = "Recurrence",
                    value = appt.recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() },
                    iconTint = M3IndigoColor,
                    iconBg = M3IndigoColor.copy(alpha = 0.10f),
                    valueBg = M3IndigoColor.copy(alpha = 0.08f),
                    valueColor = M3IndigoColor,
                )
            }

            if (appt.notes.isNotBlank()) {
                DetailDivider()
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        "Notes",
                        fontSize = 12.sp,
                        color = M3OnSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        appt.notes,
                        fontSize = 14.sp,
                        color = M3OnSurface,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AppointmentActionsSection(
    appt: Appointment,
    appointmentId: Long,
    onStartSession: () -> Unit,
    onCancel: () -> Unit,
    onNoShow: () -> Unit,
    onViewSession: () -> Unit,
    onDeleteSeries: () -> Unit,
) {
    if (appt.status == AppointmentStatus.SCHEDULED) {
        DetailSectionHeader(
            icon = Icons.Default.PlayArrow,
            title = "Actions",
            accentColor = M3GreenColor,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailActionButton(
                label = "Start Session",
                icon = Icons.Default.PlayArrow,
                color = M3Primary,
                onClick = onStartSession,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailActionButton(
                label = "Cancel",
                icon = Icons.Default.Delete,
                color = M3OnSurfaceVariant,
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            DetailActionButton(
                label = "No Show",
                icon = Icons.Default.Schedule,
                color = M3AmberColor,
                onClick = onNoShow,
                modifier = Modifier.weight(1f),
            )
        }
    }

    // View linked session
    if (appt.sessionId != null) {
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            DetailActionButton(
                label = "View Session",
                icon = Icons.Default.Visibility,
                color = M3Primary,
                onClick = onViewSession,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // Delete Series
    if (appt.recurrenceType != com.dasurv.data.local.entity.RecurrenceType.NONE ||
        appt.parentAppointmentId != null) {
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            DetailActionButton(
                label = "Delete Series",
                icon = Icons.Default.DeleteForever,
                color = M3RedColor,
                onClick = onDeleteSeries,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
