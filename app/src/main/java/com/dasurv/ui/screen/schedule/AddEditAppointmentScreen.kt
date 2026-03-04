package com.dasurv.ui.screen.schedule

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Appointment
import com.dasurv.data.local.entity.RecurrenceType
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_DATE
import com.dasurv.util.FMT_TIME
import com.dasurv.util.formatDurationMinutes
import com.dasurv.util.showDatePicker
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormDialog(
    appointmentId: Long?,
    preselectedClientId: Long?,
    preselectedDateTime: Long?,
    onDismiss: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val isEdit = appointmentId != null && appointmentId > 0
    val existingAppointment by viewModel.selectedAppointment.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val activeStaff by viewModel.activeStaff.collectAsStateWithLifecycle()

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
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var recurrenceEndDate by remember { mutableStateOf<Long?>(null) }
    var staffId by remember { mutableStateOf<Long?>(null) }
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
            recurrenceType = appt.recurrenceType
            recurrenceEndDate = appt.recurrenceEndDate
            staffId = appt.staffId
            initialized = true
        }
    }

    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat(FMT_TIME, Locale.getDefault()) }
    val cal = Calendar.getInstance().apply { timeInMillis = scheduledDateTime }

    val clientNames = remember(clients) { clients.map { it.name } }

    var isSaving by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }

    DasurvMultiPageDialog(
        title = { page ->
            when (page) {
                0 -> if (isEdit) "Edit Appointment" else "Schedule"
                else -> "Details"
            }
        },
        icon = { page ->
            when (page) {
                0 -> Icons.Default.CalendarMonth
                else -> Icons.AutoMirrored.Filled.Notes
            }
        },
        accentColor = M3AmberColor,
        pageCount = 2,
        currentPage = currentPage,
        onPageChange = { currentPage = it },
        onDismiss = onDismiss,
        isEdit = isEdit,
        confirmEnabled = selectedClientId != 0L,
        confirmLabel = { page ->
            if (page < 1) "Next" else if (isEdit) "Update" else "Book"
        },
        isSaving = isSaving,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
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
                    sessionId = existingAppointment?.sessionId,
                    recurrenceType = recurrenceType,
                    recurrenceIntervalDays = 0,
                    recurrenceEndDate = recurrenceEndDate,
                    staffId = staffId
                )
                if (!isEdit && recurrenceType != RecurrenceType.NONE && recurrenceEndDate != null) {
                    viewModel.saveRecurringAppointment(appointment) { onDismiss() }
                } else {
                    viewModel.saveAppointment(appointment) { onDismiss() }
                }
            }
        },
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (page) {
                0 -> {
                    // Page 0: Schedule
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
                            showDatePicker(context, scheduledDateTime) { scheduledDateTime = it }
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
                    DurationSelector(
                        durationMinutes = durationMinutes,
                        onDurationChange = { durationMinutes = it }
                    )
                    DasurvDropdownField(
                        value = procedureType,
                        label = "Procedure",
                        options = com.dasurv.util.PROCEDURE_TYPES,
                        onOptionSelected = { procedureType = it }
                    )
                }
                1 -> {
                    // Page 1: Details
                    if (activeStaff.isNotEmpty()) {
                        DasurvDropdownField(
                            value = activeStaff.find { it.id == staffId }?.name ?: "None",
                            label = "Assign Staff",
                            options = listOf("None") + activeStaff.map { it.name },
                            onOptionSelected = { selected ->
                                staffId = if (selected == "None") null
                                else activeStaff.find { it.name == selected }?.id
                            }
                        )
                    }
                    DasurvTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        singleLine = false,
                        minLines = 3
                    )
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
                    if (!isEdit) {
                        val recurrenceOptions = com.dasurv.util.RECURRENCE_TYPES
                        DasurvDropdownField(
                            value = recurrenceType.name.lowercase().replaceFirstChar { it.uppercase() },
                            label = "Repeat",
                            options = recurrenceOptions,
                            onOptionSelected = { selected ->
                                recurrenceType = RecurrenceType.valueOf(selected.uppercase())
                            }
                        )
                        if (recurrenceType != RecurrenceType.NONE) {
                            val endDateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }
                            FormClickableRow(
                                label = "Repeat Until",
                                value = recurrenceEndDate?.let { endDateFormat.format(Date(it)) } ?: "Select date",
                                onClick = {
                                    showDatePicker(context, recurrenceEndDate ?: (scheduledDateTime + 30 * 86400_000L)) {
                                        recurrenceEndDate = it
                                    }
                                }
                            )
                        }
                    }
                }
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
        Text("Duration", style = M3LabelStyle)

        Spacer(Modifier.height(spacing.sm + spacing.xs))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(presets.size) { index ->
                val minutes = presets[index]
                val isSelected = durationMinutes == minutes
                DasurvFilterChip(
                    label = formatDurationMinutes(minutes),
                    selected = isSelected,
                    onClick = { onDurationChange(minutes) }
                )
            }
        }
    }
}
