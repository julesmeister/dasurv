package com.dasurv.ui.screen.session

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
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
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.ColorSwatch
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
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete")
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
                    CircularProgressIndicator()
                }
            } else {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                dateFormat.format(Date(session!!.date)),
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (session!!.procedure.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Procedure: ${session!!.procedure}")
                            }
                        }
                    }

                    if (session!!.lipColorHex != null) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorSwatch(
                                    colorHex = session!!.lipColorHex!!,
                                    label = ""
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Lip Color Analysis", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        session!!.lipColorCategory ?: "Unknown",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        session!!.lipColorHex!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Equipment used in this session
                    if (sessionEquipment.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Equipment Used", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                sessionEquipment.forEach { se ->
                                    val eqName = allEquipment.find { it.id == se.equipmentId }?.name ?: "Unknown"
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(eqName, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "${se.quantityUsed.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() }} x $${String.format("%.4f", se.costPerPiece)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            "$${String.format("%.2f", se.quantityUsed * se.costPerPiece)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Equipment Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "$${String.format("%.2f", sessionEquipment.sumOf { it.quantityUsed * it.costPerPiece })}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Pigments used in this session
                    if (sessionBottleUsages.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pigments Used", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
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
                                            Text(bottleName, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "${String.format("%.2f", usage.mlUsed)} ml - $lipAreaText",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (usage.costAtTimeOfUse > 0) {
                                            Text(
                                                "$${String.format("%.2f", usage.costAtTimeOfUse)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Pigment Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "$${String.format("%.2f", sessionBottleUsages.sumOf { it.costAtTimeOfUse })}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (session!!.totalCost > 0) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Cost", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "$${String.format("%.2f", session!!.totalCost)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (session!!.durationSeconds > 0) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Session Duration", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Total: ${formatDuration(session!!.durationSeconds)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (session!!.upperLipSeconds > 0) {
                                    Text(
                                        "Upper Lip: ${formatDuration(session!!.upperLipSeconds)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                if (session!!.lowerLipSeconds > 0) {
                                    Text(
                                        "Lower Lip: ${formatDuration(session!!.lowerLipSeconds)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    if (session!!.notes.isNotBlank()) {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Notes", style = MaterialTheme.typography.titleSmall)
                                Text(session!!.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
