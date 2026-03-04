package com.dasurv.ui.screen.session

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.ColorSwatch
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvConfirmDialog
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatDurationTimer
import com.dasurv.util.formatPrecise
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    timerViewModel: SessionTimerViewModel,
    viewModel: SessionViewModel = hiltViewModel()
) {
    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    val session by viewModel.selectedSession.collectAsStateWithLifecycle()
    val sessionEquipment by viewModel.sessionEquipmentList.collectAsStateWithLifecycle()
    val allEquipment by viewModel.allEquipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val sessionBottleUsages by viewModel.sessionBottleUsages.collectAsStateWithLifecycle()
    val allBottles by viewModel.allBottles.collectAsStateWithLifecycle(initialValue = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = DasurvTheme.spacing

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
        containerColor = M3FieldBg,
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
                        .padding(vertical = spacing.lg)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    // Date & procedure card
                    M3ListCard {
                        Column(modifier = Modifier.padding(spacing.lg)) {
                            Text(
                                dateFormat.format(Date(session!!.date)),
                                style = MaterialTheme.typography.titleMedium,
                                color = M3OnSurface
                            )
                            if (session!!.procedure.isNotBlank()) {
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    "Procedure: ${session!!.procedure}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = M3OnSurface
                                )
                            }
                        }
                    }

                    // Lip color card
                    if (session!!.lipColorHex != null) {
                        M3ListCard {
                            Row(
                                modifier = Modifier.padding(spacing.lg),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorSwatch(
                                    colorHex = session!!.lipColorHex!!,
                                    label = ""
                                )
                                Spacer(modifier = Modifier.width(spacing.md))
                                Column {
                                    Text(
                                        "Lip Color Analysis",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        session!!.lipColorCategory ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        session!!.lipColorHex!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = M3OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Equipment used in this session
                    if (sessionEquipment.isNotEmpty()) {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Equipment Used",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = M3OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(spacing.sm))
                                sessionEquipment.forEach { se ->
                                    val eqName = allEquipment.find { it.id == se.equipmentId }?.name ?: "Unknown"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                eqName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = M3OnSurface
                                            )
                                            Text(
                                                "${se.quantityUsed.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }} x $${se.costPerPiece.formatPrecise()}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = M3OnSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "$${(se.quantityUsed * se.costPerPiece).formatCurrency()}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = M3OnSurface
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(spacing.sm))
                                M3ListDivider()
                                Spacer(modifier = Modifier.height(spacing.sm))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Equipment Total",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        "$${sessionEquipment.sumOf { it.quantityUsed * it.costPerPiece }.formatCurrency()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = M3Primary
                                    )
                                }
                            }
                        }
                    }

                    // Pigments used in this session
                    if (sessionBottleUsages.isNotEmpty()) {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Pigments Used",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = M3OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(spacing.sm))
                                sessionBottleUsages.forEach { usage ->
                                    val bottleName = allBottles.find { it.id == usage.bottleId }?.pigmentName ?: "Unknown"
                                    val lipAreaText = when (usage.lipArea) {
                                        UsageLipArea.UPPER -> "Upper Lip"
                                        UsageLipArea.LOWER -> "Lower Lip"
                                        UsageLipArea.BOTH -> "Both Lips"
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                bottleName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = M3OnSurface
                                            )
                                            Text(
                                                "${usage.mlUsed.formatCurrency()} ml - $lipAreaText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = M3OnSurfaceVariant
                                            )
                                        }
                                        if (usage.costAtTimeOfUse > 0) {
                                            Text(
                                                "$${usage.costAtTimeOfUse.formatCurrency()}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = M3OnSurface
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(spacing.sm))
                                M3ListDivider()
                                Spacer(modifier = Modifier.height(spacing.sm))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Pigment Total",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = M3OnSurface
                                    )
                                    Text(
                                        "$${sessionBottleUsages.sumOf { it.costAtTimeOfUse }.formatCurrency()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = M3Primary
                                    )
                                }
                            }
                        }
                    }

                    // Total cost card
                    if (session!!.totalCost > 0) {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Total Cost",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = M3OnSurfaceVariant
                                )
                                Text(
                                    "$${session!!.totalCost.formatCurrency()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = M3Primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Duration card
                    if (session!!.durationSeconds > 0) {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Session Duration",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = M3OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(spacing.sm))
                                Text(
                                    "Total: ${formatDurationTimer(session!!.durationSeconds)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = M3OnSurface
                                )
                                if (session!!.upperLipSeconds > 0) {
                                    Text(
                                        "Upper Lip: ${formatDurationTimer(session!!.upperLipSeconds)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurface
                                    )
                                }
                                if (session!!.lowerLipSeconds > 0) {
                                    Text(
                                        "Lower Lip: ${formatDurationTimer(session!!.lowerLipSeconds)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = M3OnSurface
                                    )
                                }
                            }
                        }
                    }

                    // Notes card
                    if (session!!.notes.isNotBlank()) {
                        M3ListCard {
                            Column(modifier = Modifier.padding(spacing.lg)) {
                                Text(
                                    "Notes",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = M3OnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    session!!.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = M3OnSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
