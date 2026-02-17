package com.dasurv.ui.screen.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onSessionCreated: (Long) -> Unit,
    timerViewModel: SessionTimerViewModel,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val timerState by timerViewModel.state.collectAsStateWithLifecycle()
    val equipment by viewModel.allEquipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedIds by viewModel.selectedEquipmentIds.collectAsStateWithLifecycle()
    val quantities by viewModel.equipmentQuantities.collectAsStateWithLifecycle()
    val bottles by viewModel.allBottles.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedBottleIds by viewModel.selectedBottleIds.collectAsStateWithLifecycle()
    val bottleEntries by viewModel.bottleEntries.collectAsStateWithLifecycle()

    val consumables = remember(equipment) { equipment.filter { it.type == "consumable" } }

    var procedure by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var lipColorCategory by remember { mutableStateOf("") }
    var lipColorHex by remember { mutableStateOf("") }
    var showConflictDialog by remember { mutableStateOf(false) }

    val costSummary = remember(selectedIds, equipment, quantities, selectedBottleIds, bottles, bottleEntries) {
        viewModel.calculateCost(equipment, bottles)
    }

    val isTimerForThisClient = timerState.isActive && timerState.clientId == clientId
    val isTimerForOtherClient = timerState.isActive && timerState.clientId != null && timerState.clientId != clientId

    if (showConflictDialog) {
        DasurvConfirmDialog(
            onDismissRequest = { showConflictDialog = false },
            icon = Icons.Default.Warning,
            iconTint = MaterialTheme.colorScheme.error,
            title = "Timer Already Running",
            message = "A timer is running for ${timerState.clientName.ifBlank { "another client" }}. " +
                "Starting a new timer will discard those durations.",
            confirmText = "Replace Timer",
            onConfirm = {
                showConflictDialog = false
                timerViewModel.startTimer(clientId)
            }
        )
    }

    Scaffold(
        containerColor = FormScreenBackground,
        topBar = {
            TopAppBar(
                title = { Text("New Session") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FormScreenBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FormScreenBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Procedure, Lip Color Category, Color Hex
            item {
                DasurvFormCard {
                    FormRow(
                        label = "Procedure",
                        value = procedure,
                        onValueChange = { procedure = it }
                    )
                    FormRow(
                        label = "Lip Category",
                        value = lipColorCategory,
                        onValueChange = { lipColorCategory = it }
                    )
                    FormRow(
                        label = "Color Hex",
                        value = lipColorHex,
                        onValueChange = { lipColorHex = it }
                    )
                }
            }

            // Card 2: Notes
            item {
                DasurvFormCard {
                    FormRow(
                        label = "Notes",
                        value = notes,
                        onValueChange = { notes = it },
                        singleLine = false
                    )
                }
            }

            // Timer controls (outside cards)
            item {
                SessionTimerControls(
                    isTimerForThisClient = isTimerForThisClient,
                    isTimerForOtherClient = isTimerForOtherClient,
                    timerState = timerState,
                    onPauseResume = { timerViewModel.pauseResumeTimer() },
                    onRequestStop = { timerViewModel.requestStop() },
                    onShowConflictDialog = { showConflictDialog = true },
                    onStartTimer = { timerViewModel.startTimer(clientId) },
                    onToggleUpper = { timerViewModel.toggleUpperZone() },
                    onToggleLower = { timerViewModel.toggleLowerZone() }
                )
            }

            // Consumables section (wrapped in card)
            if (consumables.isNotEmpty()) {
                item {
                    Text("Consumables Used", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    DasurvFormCard {
                        consumables.forEach { item ->
                            val isSelected = item.id in selectedIds
                            val qty = quantities[item.id] ?: 1.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleEquipment(item.id) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "$${String.format("%.4f", item.costPerPiece)} / piece" +
                                            if (item.piecesPerPackage > 1)
                                                " (${item.piecesPerPackage}/pkg)"
                                            else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    var qtyText by remember(item.id, qty) {
                                        mutableStateOf(if (qty == qty.toLong().toDouble()) qty.toLong().toString() else qty.toString())
                                    }
                                    DasurvTextField(
                                        value = qtyText,
                                        onValueChange = { newVal ->
                                            qtyText = newVal
                                            newVal.toDoubleOrNull()?.let { viewModel.setEquipmentQuantity(item.id, it) }
                                        },
                                        label = { Text("Qty") },
                                        modifier = Modifier.width(72.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Pigment bottles section (wrapped in card)
            if (bottles.isNotEmpty()) {
                item {
                    Text("Pigment Bottles Used", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    DasurvFormCard {
                        bottles.forEach { bottle ->
                            val isSelected = bottle.id in selectedBottleIds
                            val entry = bottleEntries[bottle.id]
                            val bottleColor = try {
                                Color(android.graphics.Color.parseColor(bottle.colorHex))
                            } catch (e: Exception) {
                                Color.Gray
                            }

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleBottle(bottle.id) }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(bottleColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bottle.pigmentName, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "${bottle.pigmentBrand} - ${String.format("%.1f", bottle.remainingMl)} ml left",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        var mlText by remember(bottle.id, entry?.mlUsed) {
                                            mutableStateOf(entry?.mlUsed?.let {
                                                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                                            } ?: "0.5")
                                        }
                                        DasurvTextField(
                                            value = mlText,
                                            onValueChange = { newVal ->
                                                mlText = newVal
                                                newVal.toDoubleOrNull()?.let { viewModel.setBottleMlUsed(bottle.id, it) }
                                            },
                                            label = { Text("ml") },
                                            modifier = Modifier.width(72.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Row(
                                        modifier = Modifier.padding(start = 48.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        UsageLipArea.entries.forEach { area ->
                                            FilterChip(
                                                selected = (entry?.lipArea ?: UsageLipArea.BOTH) == area,
                                                onClick = { viewModel.setBottleLipArea(bottle.id, area) },
                                                label = {
                                                    Text(
                                                        when (area) {
                                                            UsageLipArea.UPPER -> "U"
                                                            UsageLipArea.LOWER -> "L"
                                                            UsageLipArea.BOTH -> "B"
                                                        }
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (costSummary.items.isNotEmpty()) {
                item {
                    CostBreakdown(costSummary = costSummary)
                }
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val (totalDur, upperDur, lowerDur) = timerViewModel.getDurationsForClient(clientId)
                        val session = Session(
                            clientId = clientId,
                            procedure = procedure.trim(),
                            notes = notes.trim(),
                            lipColorCategory = lipColorCategory.trim().ifBlank { null },
                            lipColorHex = lipColorHex.trim().ifBlank { null },
                            totalCost = costSummary.totalCost,
                            durationSeconds = totalDur,
                            upperLipSeconds = upperDur,
                            lowerLipSeconds = lowerDur
                        )
                        viewModel.saveSession(session, equipment) { sessionId ->
                            if (isTimerForThisClient) {
                                timerViewModel.resetTimer()
                            }
                            onSessionCreated(sessionId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF263238),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text("Create Session")
                }
            }
        }
    }
}
