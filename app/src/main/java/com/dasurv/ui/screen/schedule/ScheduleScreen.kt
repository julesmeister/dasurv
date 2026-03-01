package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.data.model.AppointmentWithClient
import com.dasurv.data.model.CalendarDay
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
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
    val spacing = DasurvTheme.spacing
    val calendarMonth by viewModel.calendarMonth.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDayOfMonth.collectAsStateWithLifecycle()
    val selectedDayAppointments by viewModel.selectedDayAppointments.collectAsStateWithLifecycle()
    val year by viewModel.currentYear.collectAsStateWithLifecycle()
    val month by viewModel.currentMonth.collectAsStateWithLifecycle()

    val monthName = remember(year, month) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).run {
            val cal = Calendar.getInstance().apply { set(year, month, 1) }
            format(cal.time)
        }
    }

    Scaffold(
        containerColor = M3SurfaceContainer,
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Schedule") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = M3SurfaceContainer
                )
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = onNavigateToAddAppointment,
                contentDescription = "Add Appointment"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            // Calendar card: month navigation + grid
            item {
                M3ListCard {
                    // Month navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.sm, vertical = spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.navigateMonth(-1) }) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = M3OnSurface
                            )
                        }
                        Text(
                            text = monthName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface
                        )
                        IconButton(onClick = { viewModel.navigateMonth(1) }) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next month",
                                tint = M3OnSurface
                            )
                        }
                    }

                    // Day of week headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.sm)
                    ) {
                        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = M3OnSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.xs))

                    // Calendar grid (6 rows)
                    val rows = calendarMonth.days.chunked(7)
                    rows.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.xs)
                        ) {
                            row.forEach { day ->
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

                    Spacer(modifier = Modifier.height(spacing.xs))
                }
            }

            // Selected day appointments
            if (selectedDay != null) {
                item {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Appointments",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = M3OnSurface
                        )
                        TextButton(onClick = {
                            val cal = Calendar.getInstance().apply {
                                set(year, month, selectedDay!!, 10, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onNavigateToAddAppointmentForDay(cal.timeInMillis)
                        }) {
                            Text(
                                "+ Add",
                                color = M3Primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                if (selectedDayAppointments.isEmpty()) {
                    item {
                        DasurvEmptyState(
                            icon = Icons.Default.CalendarToday,
                            message = "No appointments for this day"
                        )
                    }
                } else {
                    item {
                        M3ListCard {
                            selectedDayAppointments.forEachIndexed { index, awc ->
                                AppointmentListRow(
                                    awc = awc,
                                    onClick = { onNavigateToAppointmentDetail(awc.appointment.id) }
                                )
                                if (index < selectedDayAppointments.lastIndex) {
                                    M3ListDivider()
                                }
                            }
                        }
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
private fun AppointmentListRow(
    awc: AppointmentWithClient,
    onClick: () -> Unit
) {
    val statusColor = when (awc.appointment.status) {
        AppointmentStatus.SCHEDULED -> M3Primary
        AppointmentStatus.COMPLETED -> M3GreenColor
        AppointmentStatus.CANCELLED -> M3OnSurfaceVariant
        AppointmentStatus.NO_SHOW -> M3RedColor
    }
    val statusContainerColor = when (awc.appointment.status) {
        AppointmentStatus.SCHEDULED -> M3PrimaryContainer
        AppointmentStatus.COMPLETED -> M3GreenContainer
        AppointmentStatus.CANCELLED -> M3FieldBg
        AppointmentStatus.NO_SHOW -> M3RedContainer
    }

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val durationText = formatDuration(awc.appointment.durationMinutes)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(statusContainerColor)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeFormat.format(Date(awc.appointment.scheduledDateTime)),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = awc.clientName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = M3OnSurface
            )
            if (awc.appointment.procedureType.isNotBlank()) {
                Text(
                    text = awc.appointment.procedureType,
                    fontSize = 12.sp,
                    color = M3OnSurfaceVariant
                )
            }
            Text(
                text = "$durationText  ·  ${awc.appointment.status.name.lowercase()
                    .replaceFirstChar { it.uppercase() }}",
                fontSize = 12.sp,
                color = M3OnSurfaceVariant
            )
        }
    }
}

private fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
