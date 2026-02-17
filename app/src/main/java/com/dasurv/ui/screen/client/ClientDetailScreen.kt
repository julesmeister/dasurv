package com.dasurv.ui.screen.client

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

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
                title = { Text(client?.name ?: "Client") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditClient(clientId) }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToNewSession(clientId) },
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Text("New Session")
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = client == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "client-detail-state"
        ) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (client!!.phone.isNotBlank()) {
                                    Text("Phone: ${client!!.phone}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (client!!.email.isNotBlank()) {
                                    Text("Email: ${client!!.email}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (client!!.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Notes:", style = MaterialTheme.typography.labelLarge)
                                    Text(client!!.notes, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    // Lip Photos section
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Lip Photos", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { onNavigateToLipCamera(clientId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Take Photo")
                                    }
                                    FilledTonalButton(
                                        onClick = { onNavigateToLipPhotoGallery(clientId) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("View All")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(
                                    onClick = { onNavigateToTryOn(clientId) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Brush, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Try On Colors")
                                }
                            }
                        }
                    }

                    // Book Appointment button
                    item {
                        FilledTonalButton(
                            onClick = { onNavigateToBookAppointment(clientId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Book Appointment")
                        }
                    }

                    // Financial Summary card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Financials",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Balance hero
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (financialSummary.balance > 0.01)
                                        MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            if (financialSummary.balance > 0.01) "Outstanding" else "Balance",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (financialSummary.balance > 0.01)
                                                MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "$${String.format("%.2f", financialSummary.balance)}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (financialSummary.balance > 0.01)
                                                MaterialTheme.colorScheme.onErrorContainer
                                            else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Charged / Paid stat row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Charged", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "$${String.format("%.2f", financialSummary.totalCharged)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Paid", style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "$${String.format("%.2f", financialSummary.totalPaid)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Upcoming Appointments (${scheduledAppointments.size})",
                                style = MaterialTheme.typography.titleMedium)
                        }
                        items(scheduledAppointments, key = { it.id }) { appointment ->
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                onClick = { onNavigateToAppointmentDetail(appointment.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = dateFormat.format(Date(appointment.scheduledDateTime)),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (appointment.procedureType.isNotBlank()) {
                                        Text(appointment.procedureType, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(
                                        "${appointment.durationMinutes} min",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sessions (${sessions.size})", style = MaterialTheme.typography.titleMedium)
                    }

                    if (sessions.isEmpty()) {
                        item {
                            Text(
                                "No sessions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(sessions, key = { it.id }) { session ->
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                onClick = { onNavigateToSession(session.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = dateFormat.format(Date(session.date)),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (session.procedure.isNotBlank()) {
                                        Text(session.procedure, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (session.totalCost > 0) {
                                        Text(
                                            "$${String.format("%.2f", session.totalCost)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
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
