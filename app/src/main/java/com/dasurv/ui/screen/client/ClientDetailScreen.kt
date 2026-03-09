package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import com.dasurv.data.model.FinancialSummary
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Payments
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.DetailSectionHeader
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3IndigoColor
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3PinkAccent
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.component.rememberSnackbarState
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToAppointmentDetail: (Long) -> Unit,
    onNavigateToLipCamera: (Long) -> Unit = {},
    onNavigateToLipPhotoGallery: (Long) -> Unit = {},
    onNavigateToTryOn: (Long) -> Unit = {},
    onNavigateToSessions: (Long) -> Unit = {},
    onNavigateToTransactions: (Long) -> Unit = {},
    onNavigateToAddUpdate: (Long) -> Unit = {},
    onNavigateToEditUpdate: (Long, Long) -> Unit = { _, _ -> },
    viewModel: ClientViewModel = hiltViewModel()
) {
    var showEditClientDialog by remember { mutableStateOf(false) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var showNewSessionDialog by remember { mutableStateOf(false) }

    if (showEditClientDialog) {
        com.dasurv.ui.screen.client.ClientFormDialog(
            clientId = clientId,
            onDismiss = {
                showEditClientDialog = false
                viewModel.loadClient(clientId)
            }
        )
    }

    if (showAppointmentDialog) {
        com.dasurv.ui.screen.schedule.AppointmentFormDialog(
            appointmentId = null,
            preselectedClientId = clientId,
            preselectedDateTime = null,
            onDismiss = { showAppointmentDialog = false }
        )
    }

    if (showNewSessionDialog) {
        com.dasurv.ui.screen.session.NewSessionDialog(
            clientId = clientId,
            onDismiss = { showNewSessionDialog = false }
        )
    }

    LaunchedEffect(clientId) { viewModel.loadClient(clientId) }

    val client by viewModel.selectedClient.collectAsStateWithLifecycle()
    val appointments by viewModel.getAppointmentsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val financialSummary by viewModel.getFinancialSummary(clientId)
        .collectAsStateWithLifecycle(initialValue = FinancialSummary())
    val clientUpdates by viewModel.getUpdatesForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val clientSessions by viewModel.getSessionsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
    val spacing = DasurvTheme.spacing

    if (showDeleteDialog && client != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Delete Client",
            message = "Delete ${client!!.name}? This will also delete all their sessions.",
            onConfirm = { viewModel.deleteClient(client!!) { onNavigateBack() } }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    DasurvTopAppBarTitle(
                        title = client?.name ?: "Client"
                    )
                },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = { showEditClientDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit", tint = M3OnSurface)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = M3OnSurface)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { showNewSessionDialog = true },
                contentDescription = "New Session"
            )
        },
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        containerColor = M3SurfaceContainer
    ) { padding ->
        AnimatedContent(
            targetState = client == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "client-detail-state"
        ) { isLoading ->
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = M3Primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(vertical = spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    if (client!!.phone.isNotBlank() || client!!.email.isNotBlank() || client!!.notes.isNotBlank()) {
                        item {
                            DetailSectionHeader(
                                icon = Icons.Default.ContactPhone,
                                title = "CONTACT INFO",
                                accentColor = M3CyanColor,
                            )
                        }
                        item { ClientContactCard(client!!) }
                    }

                    item {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        DetailSectionHeader(
                            icon = Icons.Default.CameraAlt,
                            title = "LIP PHOTOS",
                            accentColor = M3PinkAccent,
                        )
                    }
                    item {
                        LipPhotosCard(
                            clientId = clientId,
                            onNavigateToLipCamera = onNavigateToLipCamera,
                            onNavigateToLipPhotoGallery = onNavigateToLipPhotoGallery,
                            onNavigateToTryOn = onNavigateToTryOn
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        DetailSectionHeader(
                            icon = Icons.Default.CalendarMonth,
                            title = "QUICK ACTIONS",
                            accentColor = M3Primary,
                        )
                    }
                    item {
                        BookAppointmentCard(
                            clientId = clientId,
                            onNavigateToBookAppointment = { _ -> showAppointmentDialog = true },
                            onNavigateToSessions = onNavigateToSessions
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        DetailSectionHeader(
                            icon = Icons.Default.Payments,
                            title = "FINANCIALS",
                            accentColor = M3GreenColor,
                        )
                    }
                    item {
                        FinancialSummaryCard(
                            financialSummary = financialSummary,
                            clientId = clientId,
                            onNavigateToTransactions = onNavigateToTransactions
                        )
                    }

                    // Updates timeline
                    item {
                        Spacer(modifier = Modifier.height(spacing.sm))
                        UpdatesTimelineSection(
                            updates = clientUpdates,
                            sessions = clientSessions,
                            onAddUpdate = { onNavigateToAddUpdate(clientId) },
                            onEditUpdate = { update -> onNavigateToEditUpdate(clientId, update.id) },
                            onDeleteUpdate = { update -> viewModel.deleteUpdate(update) },
                        )
                    }

                    // Upcoming appointments
                    val scheduledAppointments = appointments.filter {
                        it.status == com.dasurv.data.local.entity.AppointmentStatus.SCHEDULED
                    }
                    if (scheduledAppointments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(spacing.sm))
                            DetailSectionHeader(
                                icon = Icons.Default.CalendarMonth,
                                title = "UPCOMING APPOINTMENTS (${scheduledAppointments.size})",
                                accentColor = M3AmberColor,
                            )
                        }
                        item {
                            AppointmentsList(
                                appointments = scheduledAppointments,
                                onNavigateToAppointmentDetail = onNavigateToAppointmentDetail
                            )
                        }
                    }

                }
            }
        }
    }
}
