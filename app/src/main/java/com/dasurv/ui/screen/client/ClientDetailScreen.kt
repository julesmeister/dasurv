package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import com.dasurv.data.model.FinancialSummary
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer
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
                    // Client contact info card
                    item {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                if (client!!.phone.isNotBlank()) {
                                    Text(
                                        "Phone: ${client!!.phone}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurface
                                    )
                                }
                                if (client!!.email.isNotBlank()) {
                                    Text(
                                        "Email: ${client!!.email}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurface
                                    )
                                }
                                if (client!!.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(spacing.sm))
                                    Text(
                                        "Notes:",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        client!!.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Lip Photos section
                    item {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Lip Photos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = M3OnSurface
                                )
                                Spacer(modifier = Modifier.height(spacing.sm))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                                ) {
                                    FilledTonalButton(
                                        onClick = { onNavigateToLipCamera(clientId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.CameraAlt,
                                            null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(spacing.xs))
                                        Text("Take Photo")
                                    }
                                    FilledTonalButton(
                                        onClick = { onNavigateToLipPhotoGallery(clientId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("View All")
                                    }
                                }
                                Spacer(modifier = Modifier.height(spacing.sm))
                                FilledTonalButton(
                                    onClick = { onNavigateToTryOn(clientId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Brush,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(spacing.xs))
                                    Text("Try On Colors")
                                }
                            }
                        }
                    }

                    // Book Appointment button
                    item {
                        M3ListCard {
                            FilledTonalButton(
                                onClick = { onNavigateToBookAppointment(clientId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(spacing.lg)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(spacing.xs))
                                Text("Book Appointment")
                            }
                        }
                    }

                    // Financial Summary card
                    item {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.xl)) {
                                Text(
                                    "Financials",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = M3OnSurface
                                )
                                Spacer(modifier = Modifier.height(spacing.lg))

                                // Balance hero
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (financialSummary.balance > 0.01)
                                        M3RedContainer
                                    else M3PrimaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(spacing.lg),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            if (financialSummary.balance > 0.01) "Outstanding" else "Balance",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (financialSummary.balance > 0.01)
                                                M3RedColor
                                            else M3Primary
                                        )
                                        Text(
                                            "$${String.format("%.2f", financialSummary.balance)}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (financialSummary.balance > 0.01)
                                                M3RedColor
                                            else M3Primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(spacing.md))

                                // Charged / Paid stat row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF0F1FA)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(spacing.md),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "Charged",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = M3OnSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "$${String.format("%.2f", financialSummary.totalCharged)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = M3OnSurface
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF0F1FA)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(spacing.md),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "Paid",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = M3OnSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "$${String.format("%.2f", financialSummary.totalPaid)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = M3OnSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(spacing.md))

                                FilledTonalButton(
                                    onClick = { onNavigateToTransactions(clientId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("View All Transactions")
                                }
                            }
                        }
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
                            M3ListCard {
                                val appointmentDateFormat = remember {
                                    SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
                                }
                                scheduledAppointments.forEachIndexed { index, appointment ->
                                    Surface(
                                        onClick = { onNavigateToAppointmentDetail(appointment.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = androidx.compose.ui.graphics.Color.Transparent
                                    ) {
                                        Column(modifier = Modifier.padding(spacing.lg)) {
                                            Text(
                                                text = appointmentDateFormat.format(Date(appointment.scheduledDateTime)),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = M3OnSurface
                                            )
                                            if (appointment.procedureType.isNotBlank()) {
                                                Text(
                                                    appointment.procedureType,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = M3OnSurface
                                                )
                                            }
                                            Text(
                                                "${appointment.durationMinutes} min",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = M3OnSurfaceVariant
                                            )
                                        }
                                    }
                                    if (index < scheduledAppointments.lastIndex) {
                                        M3ListDivider()
                                    }
                                }
                            }
                        }
                    }

                    // Sessions header
                    item {
                        Spacer(modifier = Modifier.height(spacing.sm))
                        Text(
                            "Sessions (${sessions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = M3OnSurface,
                            modifier = Modifier.padding(horizontal = spacing.lg)
                        )
                    }

                    if (sessions.isEmpty()) {
                        item {
                            DasurvEmptyState(
                                icon = Icons.Default.EventNote,
                                message = "No sessions yet"
                            )
                        }
                    } else {
                        item {
                            M3ListCard {
                                val sessionDateFormat = remember {
                                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                }
                                sessions.forEachIndexed { index, session ->
                                    Surface(
                                        onClick = { onNavigateToSession(session.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = androidx.compose.ui.graphics.Color.Transparent
                                    ) {
                                        Column(modifier = Modifier.padding(spacing.lg)) {
                                            Text(
                                                text = sessionDateFormat.format(Date(session.date)),
                                                style = MaterialTheme.typography.titleSmall,
                                                color = M3OnSurface
                                            )
                                            if (session.procedure.isNotBlank()) {
                                                Text(
                                                    session.procedure,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = M3OnSurface
                                                )
                                            }
                                            if (session.totalCost > 0) {
                                                Text(
                                                    "$${String.format("%.2f", session.totalCost)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = M3Primary
                                                )
                                            }
                                        }
                                    }
                                    if (index < sessions.lastIndex) {
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
}
