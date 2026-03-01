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
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditClient: (Long) -> Unit,
    onNavigateToSession: (Long) -> Unit,
    onNavigateToNewSession: (Long) -> Unit,
    onNavigateToBookAppointment: (Long) -> Unit,
    onNavigateToAppointmentDetail: (Long) -> Unit,
    onNavigateToLipCamera: (Long) -> Unit = {},
    onNavigateToLipPhotoGallery: (Long) -> Unit = {},
    onNavigateToTryOn: (Long) -> Unit = {},
    onNavigateToTransactions: (Long) -> Unit = {},
    viewModel: ClientViewModel = hiltViewModel()
) {
    LaunchedEffect(clientId) { viewModel.loadClient(clientId) }

    val client by viewModel.selectedClient.collectAsStateWithLifecycle()
    val sessions by viewModel.getSessionsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val appointments by viewModel.getAppointmentsForClient(clientId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val financialSummary by viewModel.getFinancialSummary(clientId)
        .collectAsStateWithLifecycle(initialValue = FinancialSummary())
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
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
                    IconButton(onClick = { onNavigateToEditClient(clientId) }) {
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
                onClick = { onNavigateToNewSession(clientId) },
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
                    item { ClientContactCard(client!!) }

                    item {
                        LipPhotosCard(
                            clientId = clientId,
                            onNavigateToLipCamera = onNavigateToLipCamera,
                            onNavigateToLipPhotoGallery = onNavigateToLipPhotoGallery,
                            onNavigateToTryOn = onNavigateToTryOn
                        )
                    }

                    item {
                        BookAppointmentCard(
                            clientId = clientId,
                            onNavigateToBookAppointment = onNavigateToBookAppointment
                        )
                    }

                    item {
                        FinancialSummaryCard(
                            financialSummary = financialSummary,
                            clientId = clientId,
                            onNavigateToTransactions = onNavigateToTransactions
                        )
                    }

                    // Upcoming appointments
                    val scheduledAppointments = appointments.filter {
                        it.status == com.dasurv.data.local.entity.AppointmentStatus.SCHEDULED
                    }
                    if (scheduledAppointments.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(spacing.sm))
                            Text(
                                "Upcoming Appointments (${scheduledAppointments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = M3OnSurface,
                                modifier = Modifier.padding(horizontal = spacing.lg)
                            )
                        }
                        item {
                            AppointmentsList(
                                appointments = scheduledAppointments,
                                onNavigateToAppointmentDetail = onNavigateToAppointmentDetail
                            )
                        }
                    }

                    // Sessions
                    item {
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            "Sessions (${sessions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = M3OnSurface,
                            modifier = Modifier.padding(horizontal = spacing.lg)
                        )
                    }

                    item {
                        SessionsList(
                            sessions = sessions,
                            onNavigateToSession = onNavigateToSession
                        )
                    }
                }
            }
        }
    }
}
