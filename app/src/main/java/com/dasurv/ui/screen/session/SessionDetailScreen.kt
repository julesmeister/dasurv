package com.dasurv.ui.screen.session

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.component.ColorSwatch
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvConfirmDialog
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.DetailActionButton
import com.dasurv.ui.component.DetailDivider
import com.dasurv.ui.component.DetailSectionHeader
import com.dasurv.ui.component.DetailValueRow
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3CyanColor
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3PinkAccent
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.component.rememberSnackbarState
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatDurationTimer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddUpdate: (clientId: Long, sessionId: Long) -> Unit = { _, _ -> },
    onNavigateToEditUpdate: (clientId: Long, updateId: Long) -> Unit = { _, _ -> },
    timerViewModel: SessionTimerViewModel,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val sessionEquipment by viewModel.sessionEquipmentList.collectAsStateWithLifecycle()
    val allEquipment by viewModel.allEquipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessionBottleUsages by viewModel.sessionBottleUsages.collectAsStateWithLifecycle()
    val allBottles by viewModel.allBottles.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeStaff by viewModel.activeStaff.collectAsStateWithLifecycle()
    val sessionUpdates by viewModel.sessionUpdates.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (showDeleteDialog && session != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.DeleteForever,
            title = "Delete Session",
            message = "Delete this session?",
            onConfirm = { viewModel.deleteSession(session!!) { onNavigateBack() } }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Session Details") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = M3OnSurfaceVariant)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = session == null,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "session-detail-state"
        ) { isLoading ->
            if (isLoading) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = M3Primary)
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Session Info ──────────────────────────────────
                    DetailSectionHeader(
                        icon = Icons.Default.CalendarToday,
                        title = "Session Info",
                    )

                    M3ListCard {
                        Column {
                            DetailValueRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Date",
                                value = dateFormat.format(Date(session!!.date)),
                                iconTint = M3Primary,
                                iconBg = M3Primary.copy(alpha = 0.10f),
                                valueBg = M3Primary.copy(alpha = 0.08f),
                                valueColor = M3Primary,
                            )
                            if (session!!.procedure.isNotBlank()) {
                                DetailDivider()
                                DetailValueRow(
                                    icon = Icons.Default.MedicalServices,
                                    label = "Procedure",
                                    value = session!!.procedure,
                                )
                            }
                            if (session!!.staffId != null) {
                                val staffName = activeStaff.find { it.id == session!!.staffId }?.name
                                if (staffName != null) {
                                    DetailDivider()
                                    DetailValueRow(
                                        icon = Icons.Default.Person,
                                        label = "Staff",
                                        value = staffName,
                                        iconTint = M3AmberColor,
                                        iconBg = M3AmberColor.copy(alpha = 0.10f),
                                        valueBg = M3AmberColor.copy(alpha = 0.08f),
                                        valueColor = M3AmberColor,
                                    )
                                }
                            }
                        }
                    }

                    // ── Lip Color Analysis ────────────────────────────
                    if (session!!.lipColorHex != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailSectionHeader(
                            icon = Icons.Default.ColorLens,
                            title = "Lip Color Analysis",
                            accentColor = M3PinkAccent,
                        )

                        M3ListCard {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ColorSwatch(
                                    colorHex = session!!.lipColorHex!!,
                                    label = "",
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        session!!.lipColorCategory ?: "Unknown",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3OnSurface,
                                    )
                                    Text(
                                        session!!.lipColorHex!!,
                                        fontSize = 13.sp,
                                        color = M3OnSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    // ── Equipment Used ────────────────────────────────
                    if (sessionEquipment.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailSectionHeader(
                            icon = Icons.Default.Build,
                            title = "Equipment Used (${sessionEquipment.size})",
                            accentColor = M3AmberColor,
                        )
                        SessionEquipmentCard(
                            sessionEquipment = sessionEquipment,
                            allEquipment = allEquipment,
                        )
                    }

                    // ── Pigments Used ─────────────────────────────────
                    if (sessionBottleUsages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailSectionHeader(
                            icon = Icons.Default.Palette,
                            title = "Pigments Used (${sessionBottleUsages.size})",
                            accentColor = M3PinkAccent,
                        )
                        SessionPigmentCard(
                            sessionBottleUsages = sessionBottleUsages,
                            allBottles = allBottles,
                        )
                    }

                    // ── Cost & Duration ───────────────────────────────
                    if (session!!.totalCost > 0 || session!!.durationSeconds > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailSectionHeader(
                            icon = Icons.Default.Payments,
                            title = "Cost & Duration",
                            accentColor = M3GreenColor,
                        )

                        M3ListCard {
                            Column {
                                if (session!!.totalCost > 0) {
                                    DetailValueRow(
                                        icon = Icons.Default.Payments,
                                        label = "Total Cost",
                                        value = "₱${session!!.totalCost.formatCurrency()}",
                                        iconTint = M3GreenColor,
                                        iconBg = M3GreenColor.copy(alpha = 0.10f),
                                        valueBg = M3GreenColor.copy(alpha = 0.08f),
                                        valueColor = M3GreenColor,
                                    )
                                }
                                if (session!!.totalCost > 0 && session!!.durationSeconds > 0) {
                                    DetailDivider()
                                }
                                if (session!!.durationSeconds > 0) {
                                    DetailValueRow(
                                        icon = Icons.Default.AccessTime,
                                        label = "Total Duration",
                                        value = formatDurationTimer(session!!.durationSeconds),
                                    )
                                    if (session!!.upperLipSeconds > 0) {
                                        DetailDivider()
                                        DetailValueRow(
                                            icon = Icons.Default.AccessTime,
                                            label = "Upper Lip",
                                            value = formatDurationTimer(session!!.upperLipSeconds),
                                            iconTint = M3OnSurfaceVariant,
                                            iconBg = M3OnSurfaceVariant.copy(alpha = 0.08f),
                                            valueBg = M3OnSurfaceVariant.copy(alpha = 0.06f),
                                            valueColor = M3OnSurfaceVariant,
                                        )
                                    }
                                    if (session!!.lowerLipSeconds > 0) {
                                        DetailDivider()
                                        DetailValueRow(
                                            icon = Icons.Default.AccessTime,
                                            label = "Lower Lip",
                                            value = formatDurationTimer(session!!.lowerLipSeconds),
                                            iconTint = M3OnSurfaceVariant,
                                            iconBg = M3OnSurfaceVariant.copy(alpha = 0.08f),
                                            valueBg = M3OnSurfaceVariant.copy(alpha = 0.06f),
                                            valueColor = M3OnSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Tags / Updates ────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    DetailSectionHeader(
                        icon = Icons.AutoMirrored.Filled.Label,
                        title = "Tags & Updates (${sessionUpdates.size})",
                        accentColor = M3CyanColor,
                    )

                    SessionUpdatesCard(
                        sessionUpdates = sessionUpdates,
                        onAddUpdate = {
                            if (session != null) {
                                onNavigateToAddUpdate(session!!.clientId, sessionId)
                            }
                        },
                        onEditUpdate = { update ->
                            if (session != null) {
                                onNavigateToEditUpdate(session!!.clientId, update.id)
                            }
                        },
                        onDeleteUpdate = { update -> viewModel.deleteUpdate(update) },
                    )

                    // ── Notes ─────────────────────────────────────────
                    if (session!!.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailSectionHeader(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            title = "Notes",
                        )

                        M3ListCard {
                            Text(
                                session!!.notes,
                                modifier = Modifier.padding(16.dp),
                                fontSize = 14.sp,
                                color = M3OnSurface,
                            )
                        }
                    }

                    // ── Delete Action ─────────────────────────────────
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DetailActionButton(
                            label = "Delete Session",
                            icon = Icons.Default.DeleteForever,
                            color = M3RedColor,
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
