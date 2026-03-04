package com.dasurv.ui.screen.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.AppointmentStatus
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_TIME
import com.dasurv.ui.util.statusColor
import com.dasurv.ui.util.statusContainerColor
import java.text.SimpleDateFormat
import java.util.*

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
    val spacing = DasurvTheme.spacing
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
        containerColor = M3SurfaceContainer,
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
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = M3Primary)
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()) }
            val timeFormat = remember { SimpleDateFormat(FMT_TIME, Locale.getDefault()) }

            val statusColor = appt.status.statusColor()
            val statusContainerColor = appt.status.statusContainerColor()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg)
            ) {
                // Main info card
                M3ListCard {
                    // Client name header
                    Column(
                        modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.lg)
                    ) {
                        Text(
                            text = clientName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3OnSurface
                        )
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = dateFormat.format(Date(appt.scheduledDateTime)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = M3OnSurfaceVariant
                        )
                    }

                    M3ListDivider()

                    // Time + duration row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.lg, vertical = spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Time",
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant
                            )
                            Text(
                                text = timeFormat.format(Date(appt.scheduledDateTime)),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = M3OnSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Duration",
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant
                            )
                            Text(
                                text = "${appt.durationMinutes} min",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = M3OnSurface
                            )
                        }
                    }

                    if (appt.procedureType.isNotBlank()) {
                        M3ListDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.lg, vertical = spacing.md)
                        ) {
                            Column {
                                Text(
                                    text = "Procedure",
                                    fontSize = 12.sp,
                                    color = M3OnSurfaceVariant
                                )
                                Text(
                                    text = appt.procedureType,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = M3OnSurface
                                )
                            }
                        }
                    }

                    if (appt.notes.isNotBlank()) {
                        M3ListDivider()
                        Column(
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md)
                        ) {
                            Text(
                                text = "Notes",
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = appt.notes,
                                fontSize = 14.sp,
                                color = M3OnSurface
                            )
                        }
                    }

                    M3ListDivider()

                    // Status row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.lg, vertical = spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Status",
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(statusContainerColor)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = appt.status.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }
                        Box {
                            TextButton(onClick = { showStatusMenu = true }) {
                                Text(
                                    "Change",
                                    color = M3Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false },
                                scrollState = rememberScrollState(),
                                shadowElevation = 0.dp
                            ) {
                                AppointmentStatus.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                status.name.lowercase()
                                                    .replaceFirstChar { it.uppercase() },
                                                color = M3OnSurface
                                            )
                                        },
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
                        M3ListDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.lg, vertical = spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = M3OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(spacing.sm))
                            Text(
                                text = "Reminder ${appt.reminderMinutesBefore} min before",
                                fontSize = 13.sp,
                                color = M3OnSurfaceVariant
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3Primary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            "Start Session",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // View linked session
                if (appt.sessionId != null) {
                    FilledTonalButton(
                        onClick = { onNavigateToSession(appt.sessionId!!) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Text(
                            "View Session",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Cancel / No Show buttons
                if (appt.status == AppointmentStatus.SCHEDULED) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateAppointmentStatus(appt, AppointmentStatus.CANCELLED) {
                                    viewModel.loadAppointment(appointmentId)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = M3OnSurface
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Medium)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.updateAppointmentStatus(appt, AppointmentStatus.NO_SHOW) {
                                    viewModel.loadAppointment(appointmentId)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = M3AmberColor
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text("No Show", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
