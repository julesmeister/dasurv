package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.AppointmentStatus
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Reload when screen resumes (including returning from edit)
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadAppointment(appointmentId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val appointment by viewModel.selectedAppointment.collectAsStateWithLifecycle()
    var clientName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    LaunchedEffect(appointment?.clientId) {
        appointment?.clientId?.let { cid ->
            viewModel.getClientName(cid).collect { clientName = it }
        }
    }

    if (showDeleteDialog && appointment != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.DeleteForever,
            title = "Delete Appointment",
            message = "Delete this appointment with $clientName?",
            onConfirm = { viewModel.deleteAppointment(appointment!!) { onNavigateBack() } }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = { Text("Appointment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(appointmentId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        }
    ) { padding ->
        val appt = appointment
        if (appt == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(clientName, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            dateFormat.format(Date(appt.scheduledDateTime)),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${timeFormat.format(Date(appt.scheduledDateTime))} - ${appt.durationMinutes} min",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (appt.procedureType.isNotBlank()) {
                            Text("Procedure: ${appt.procedureType}", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (appt.notes.isNotBlank()) {
                            Text("Notes: ${appt.notes}", style = MaterialTheme.typography.bodyMedium)
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: ${appt.status.name}", style = MaterialTheme.typography.bodyMedium)
                            Box {
                                TextButton(onClick = { showStatusMenu = true }) {
                                    Text("Change")
                                }
                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false },
                                    scrollState = rememberScrollState(),
                                    shadowElevation = 0.dp
                                ) {
                                    AppointmentStatus.entries.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status.name) },
                                            onClick = {
                                                showStatusMenu = false
                                                viewModel.updateAppointmentStatus(appt, status) {
                                                    viewModel.loadAppointment(appointmentId)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (appt.reminderEnabled) {
                            Text(
                                "Reminder: ${appt.reminderMinutesBefore} min before",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Start Session button
                if (appt.status == AppointmentStatus.SCHEDULED) {
                    Button(
                        onClick = {
                            viewModel.convertToSession(appt) { sessionId ->
                                onNavigateToSession(sessionId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Session")
                    }
                }

                // View linked session
                if (appt.sessionId != null) {
                    FilledTonalButton(
                        onClick = { onNavigateToSession(appt.sessionId!!) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Session")
                    }
                }

                // Cancel / No Show buttons
                if (appt.status == AppointmentStatus.SCHEDULED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.updateAppointmentStatus(appt, AppointmentStatus.CANCELLED) {
                                    viewModel.loadAppointment(appointmentId)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        FilledTonalButton(
                            onClick = {
                                viewModel.updateAppointmentStatus(appt, AppointmentStatus.NO_SHOW) {
                                    viewModel.loadAppointment(appointmentId)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("No Show")
                        }
                    }
                }
            }
        }
    }
}
