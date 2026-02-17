package com.dasurv.ui.screen.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Appointment
import com.dasurv.ui.component.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAppointmentScreen(
    appointmentId: Long?,
    preselectedClientId: Long?,
    preselectedDateTime: Long?,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val isEdit = appointmentId != null && appointmentId > 0
    val existingAppointment by viewModel.selectedAppointment.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()

    LaunchedEffect(appointmentId) {
        if (isEdit) viewModel.loadAppointment(appointmentId!!)
    }

    val initialDateTime = preselectedDateTime ?: System.currentTimeMillis() + 3600_000L
    var selectedClientId by remember { mutableStateOf(preselectedClientId ?: 0L) }
    var scheduledDateTime by remember { mutableLongStateOf(initialDateTime) }
    var durationMinutes by remember { mutableIntStateOf(60) }
    var procedureType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderMinutesBefore by remember { mutableIntStateOf(30) }
    var initialized by remember { mutableStateOf(!isEdit) }

    LaunchedEffect(existingAppointment) {
        val appt = existingAppointment
        if (isEdit && appt != null && !initialized) {
            selectedClientId = appt.clientId
            scheduledDateTime = appt.scheduledDateTime
            durationMinutes = appt.durationMinutes
            procedureType = appt.procedureType
            notes = appt.notes
            reminderEnabled = appt.reminderEnabled
            reminderMinutesBefore = appt.reminderMinutesBefore
            initialized = true
        }
    }

    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val cal = Calendar.getInstance().apply { timeInMillis = scheduledDateTime }

    val clientNames = remember(clients) { clients.map { it.name } }

    DasurvFormScaffold(
        title = if (isEdit) "Edit Appointment" else "New Appointment",
        onNavigateBack = onNavigateBack,
        saveText = if (isEdit) "Update" else "Book Appointment",
        saveEnabled = selectedClientId != 0L,
        snackbarMessage = if (isEdit) "Appointment updated" else "Appointment booked",
        onSave = { onDone ->
            val appointment = Appointment(
                id = if (isEdit) appointmentId!! else 0,
                clientId = selectedClientId,
                scheduledDateTime = scheduledDateTime,
                durationMinutes = durationMinutes,
                procedureType = procedureType,
                notes = notes,
                reminderEnabled = reminderEnabled,
                reminderMinutesBefore = reminderMinutesBefore,
                status = existingAppointment?.status
                    ?: com.dasurv.data.local.entity.AppointmentStatus.SCHEDULED,
                sessionId = existingAppointment?.sessionId
            )
            viewModel.saveAppointment(appointment) { onDone() }
        }
    ) {
        // Card 1: Client, Date, Time
        DasurvFormCard {
            FormDropdownRow(
                label = "Client *",
                value = clients.find { it.id == selectedClientId }?.name ?: "",
                options = clientNames,
                onOptionSelected = { selected ->
                    clients.find { it.name == selected }?.let { selectedClientId = it.id }
                }
            )
            FormClickableRow(
                label = "Date",
                value = dateFormat.format(Date(scheduledDateTime)),
                onClick = {
                    DatePickerDialog(context, { _, y, m, d ->
                        val c = Calendar.getInstance().apply {
                            timeInMillis = scheduledDateTime
                            set(y, m, d)
                        }
                        scheduledDateTime = c.timeInMillis
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }
            )
            FormClickableRow(
                label = "Time",
                value = timeFormat.format(Date(scheduledDateTime)),
                onClick = {
                    TimePickerDialog(context, { _, h, m ->
                        val c = Calendar.getInstance().apply {
                            timeInMillis = scheduledDateTime
                            set(Calendar.HOUR_OF_DAY, h)
                            set(Calendar.MINUTE, m)
                        }
                        scheduledDateTime = c.timeInMillis
                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
                }
            )
        }

        // Card 2: Duration, Procedure Type
        DasurvFormCard {
            DurationSelector(
                durationMinutes = durationMinutes,
                onDurationChange = { durationMinutes = it }
            )
            FormDropdownRow(
                label = "Procedure",
                value = procedureType,
                options = listOf("Lip Blush", "Lip Neutralizer", "Lip Combo"),
                onOptionSelected = { procedureType = it }
            )
        }

        // Card 3: Notes
        DasurvFormCard {
            FormRow(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it },
                singleLine = false
            )
        }

        // Card 4: Reminder
        DasurvFormCard {
            FormToggleRow(
                label = "Reminder",
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
            )
            if (reminderEnabled) {
                FormRow(
                    label = "Minutes before",
                    value = reminderMinutesBefore.toString(),
                    onValueChange = { reminderMinutesBefore = it.toIntOrNull() ?: 30 },
                    keyboardType = KeyboardType.Number
                )
            }
        }
    }
}

@Composable
private fun DurationSelector(
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit
) {
    val presets = listOf(15, 30, 45, 60, 90, 120, 150, 180, 210, 240, 300, 360)

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            "Duration",
            style = FormDefaults.LabelStyle
        )

        Spacer(Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(presets.size) { index ->
                val minutes = presets[index]
                val isSelected = durationMinutes == minutes
                FilterChip(
                    selected = isSelected,
                    onClick = { onDurationChange(minutes) },
                    label = {
                        Text(
                            formatDuration(minutes),
                            style = if (isSelected) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            else MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = if (isSelected) Modifier.height(40.dp) else Modifier.height(32.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
