package com.dasurv.ui.screen.schedule

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Appointment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
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
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
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
            DasurvDropdownField(
                value = clients.find { it.id == selectedClientId }?.name ?: "",
                label = "Client *",
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
            DasurvDropdownField(
                value = procedureType,
                label = "Procedure",
                options = listOf("Lip Blush", "Lip Neutralizer", "Lip Combo"),
                onOptionSelected = { procedureType = it }
            )
        }

        // Card 3: Notes
        DasurvFormCard {
            DasurvTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                minLines = 2
            )
        }

        // Card 4: Reminder
        DasurvFormCard {
            DasurvSwitchRow(
                label = "Reminder",
                checked = reminderEnabled,
                onCheckedChange = { reminderEnabled = it }
            )
            if (reminderEnabled) {
                DasurvTextField(
                    value = reminderMinutesBefore.toString(),
                    onValueChange = { reminderMinutesBefore = it.toIntOrNull() ?: 30 },
                    label = "Minutes before",
                    autoCapitalize = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
    val spacing = DasurvTheme.spacing
    val presets = listOf(15, 30, 45, 60, 90, 120, 150, 180, 210, 240, 300, 360)

    Column(modifier = Modifier.padding(vertical = spacing.md)) {
        Text(
            "Duration",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant,
        )

        Spacer(Modifier.height(spacing.sm + spacing.xs))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(presets.size) { index ->
                val minutes = presets[index]
                val isSelected = durationMinutes == minutes
                FilledTonalButton(
                    onClick = { onDurationChange(minutes) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) M3PrimaryContainer else M3FieldBg,
                        contentColor = if (isSelected) M3Primary else M3OnSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm)
                ) {
                    Text(
                        formatDuration(minutes),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
