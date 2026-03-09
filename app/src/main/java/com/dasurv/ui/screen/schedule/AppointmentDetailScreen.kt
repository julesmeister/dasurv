package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.ui.component.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        AppointmentFormDialog(
            appointmentId = appointmentId,
            preselectedClientId = null,
            preselectedDateTime = null,
            onDismiss = {
                showEditDialog = false
                viewModel.loadAppointment(appointmentId)
            }
        )
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadAppointment(appointmentId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
    val appointment by viewModel.selectedAppointment.collectAsStateWithLifecycle()
    val activeStaff by viewModel.activeStaff.collectAsStateWithLifecycle()
    var clientName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteSeriesDialog by remember { mutableStateOf(false) }
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

    if (showDeleteSeriesDialog && appointment != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteSeriesDialog = false },
            icon = Icons.Default.DeleteForever,
            title = "Delete Series",
            message = "Delete this appointment and all recurring occurrences?",
            onConfirm = { viewModel.deleteAppointmentSeries(appointment!!) { onNavigateBack() } }
        )
    }

    Scaffold(
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Appointment") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = M3OnSurface)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = M3RedColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = M3SurfaceContainer
                )
            )
        }
    ) { padding ->
        val appt = appointment
        if (appt == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = M3Primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                DetailSectionHeader(
                    icon = Icons.Default.CalendarToday,
                    title = "Appointment Info",
                )

                AppointmentInfoCard(
                    appt = appt,
                    clientName = clientName,
                    activeStaff = activeStaff,
                    showStatusMenu = showStatusMenu,
                    onStatusMenuToggle = { showStatusMenu = it },
                    onStatusChange = { status ->
                        viewModel.updateAppointmentStatus(appt, status) {
                            viewModel.loadAppointment(appointmentId)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppointmentActionsSection(
                    appt = appt,
                    appointmentId = appointmentId,
                    onStartSession = {
                        viewModel.convertToSession(appt) { sessionId ->
                            onNavigateToSession(sessionId)
                        }
                    },
                    onCancel = {
                        viewModel.updateAppointmentStatus(appt, AppointmentStatus.CANCELLED) {
                            viewModel.loadAppointment(appointmentId)
                        }
                    },
                    onNoShow = {
                        viewModel.updateAppointmentStatus(appt, AppointmentStatus.NO_SHOW) {
                            viewModel.loadAppointment(appointmentId)
                        }
                    },
                    onViewSession = { onNavigateToSession(appt.sessionId!!) },
                    onDeleteSeries = { showDeleteSeriesDialog = true },
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
