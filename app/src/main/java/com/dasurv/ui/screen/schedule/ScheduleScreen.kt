package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.model.CalendarDay
import com.dasurv.ui.component.CardPosition
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddAppointment: () -> Unit,
    onNavigateToAddAppointmentForDay: (Long) -> Unit,
    onNavigateToAppointmentDetail: (Long) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfMonth.collectAsStateWithLifecycle()
    val selectedDayAppointments by viewModel.selectedDayAppointments.collectAsStateWithLifecycle()
    val year by viewModel.currentYear.collectAsStateWithLifecycle()
    val month by viewModel.currentMonth.collectAsStateWithLifecycle()

    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).run {
        val cal = Calendar.getInstance().apply { set(year, month, 1) }
        format(cal.time)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddAppointment,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Default.Add, "Add Appointment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Month navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                        Icon(Icons.Default.ChevronLeft, "Previous month")
                    }
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { viewModel.navigateMonth(1) }) {
                        Icon(Icons.Default.ChevronRight, "Next month")
                    }
                }
            }

            // Day of week headers
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calendar grid (6 rows)
            val rows = calendarMonth.days.chunked(7)
            items(rows.size) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    rows[rowIndex].forEach { day ->
                        CalendarDayCell(
                            day = day,
                            isSelected = day.isCurrentMonth && day.dayOfMonth == selectedDay,
                            onClick = {
                                if (day.isCurrentMonth) viewModel.selectDay(day.dayOfMonth)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Selected day appointments
            if (selectedDay != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Appointments",
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(onClick = {
                            val cal = Calendar.getInstance().apply {
                                set(year, month, selectedDay!!, 10, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onNavigateToAddAppointmentForDay(cal.timeInMillis)
                        }) {
                            Text("+ Add")
                        }
                    }
                }

                if (selectedDayAppointments.isEmpty()) {
                    item {
                        Text(
                            "No appointments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(selectedDayAppointments.size, key = { selectedDayAppointments[it].appointment.id }) { index ->
                        val awc = selectedDayAppointments[index]
                        val position = when {
                            selectedDayAppointments.size == 1 -> CardPosition.Only
                            index == 0 -> CardPosition.First
                            index == selectedDayAppointments.lastIndex -> CardPosition.Last
                            else -> CardPosition.Middle
                        }
                        AppointmentCard(
                            awc = awc,
                            onClick = { onNavigateToAppointmentDetail(awc.appointment.id) },
                            position = position
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.small
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        day.isToday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(bgColor)
            .clickable(enabled = day.isCurrentMonth, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = textColor
            )
            if (day.appointments.isNotEmpty() && day.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(
    awc: AppointmentWithClient,
    onClick: () -> Unit,
    position: CardPosition
) {
    val largeRadius = 24.dp
    val smallRadius = 6.dp

    val shape = when (position) {
        CardPosition.Only -> RoundedCornerShape(largeRadius)
        CardPosition.First -> RoundedCornerShape(
            topStart = largeRadius, topEnd = largeRadius,
            bottomStart = smallRadius, bottomEnd = smallRadius
        )
        CardPosition.Middle -> RoundedCornerShape(smallRadius)
        CardPosition.Last -> RoundedCornerShape(
            topStart = smallRadius, topEnd = smallRadius,
            bottomStart = largeRadius, bottomEnd = largeRadius
        )
    }

    val statusColor = when (awc.appointment.status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        AppointmentStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        AppointmentStatus.CANCELLED -> MaterialTheme.colorScheme.outline
        AppointmentStatus.NO_SHOW -> MaterialTheme.colorScheme.error
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val durationText = formatDuration(awc.appointment.durationMinutes)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    timeFormat.format(Date(awc.appointment.scheduledDateTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = awc.clientName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (awc.appointment.procedureType.isNotBlank()) {
                    Text(
                        text = awc.appointment.procedureType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$durationText  ·  ${awc.appointment.status.name.lowercase()
                        .replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
