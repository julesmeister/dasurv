package com.dasurv.ui.screen.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Session
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatMl
import com.dasurv.util.formatPrecise

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
            iconTint = M3RedColor,
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
        containerColor = M3FieldBg,
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("New Session") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = M3FieldBg
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(M3FieldBg),
            contentPadding = PaddingValues(DasurvTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.lg)
        ) {
            // Card 1: Procedure, Lip Color Category, Color Hex
            item {
                DasurvFormCard {
                    DasurvDropdownField(
                        value = procedure,
                        label = "Procedure",
                        options = listOf("Lip Blush", "Lip Neutralizer", "Lip Combo"),
                        onOptionSelected = { procedure = it }
                    )
                    DasurvTextField(
                        value = lipColorCategory,
                        onValueChange = { lipColorCategory = it },
                        label = "Lip Category"
                    )
                    DasurvTextField(
                        value = lipColorHex,
                        onValueChange = { lipColorHex = it },
                        label = "Color Hex",
                        autoCapitalize = false
                    )
                }
            }

            // Card 2: Notes
            item {
                DasurvFormCard {
                    DasurvTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        singleLine = false,
                        minLines = 2
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
                    Text(
                        "Consumables Used",
                        style = MaterialTheme.typography.titleMedium,
                        color = M3OnSurface
                    )
                }

                item {
                    DasurvFormCard {
                        consumables.forEach { item ->
                            val isSelected = item.id in selectedIds
                            val qty = quantities[item.id] ?: 1.0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = DasurvTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.toggleEquipment(item.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = M3Primary)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = M3OnSurface)
                                    Text(
                                        "$${item.costPerPiece.formatPrecise()} / piece" +
                                            if (item.piecesPerPackage > 1)
                                                " (${item.piecesPerPackage}/pkg)"
                                            else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = M3OnSurfaceVariant
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
                                        label = "Qty",
                                        modifier = Modifier.width(72.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        autoCapitalize = false
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
                    Text(
                        "Pigment Bottles Used",
                        style = MaterialTheme.typography.titleMedium,
                        color = M3OnSurface
                    )
                }

                item {
                    DasurvFormCard {
                        bottles.forEach { bottle ->
                            val isSelected = bottle.id in selectedBottleIds
                            val entry = bottleEntries[bottle.id]
                            val bottleColor = remember(bottle.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(bottle.colorHex))
                                } catch (e: Exception) {
                                    Color.Gray
                                }
                            }

                            Column(modifier = Modifier.padding(vertical = DasurvTheme.spacing.xs)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.toggleBottle(bottle.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = M3Primary)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(bottleColor)
                                    )
                                    Spacer(modifier = Modifier.width(DasurvTheme.spacing.sm))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bottle.pigmentName, style = MaterialTheme.typography.bodyMedium, color = M3OnSurface)
                                        Text(
                                            "${bottle.pigmentBrand} - ${bottle.remainingMl.formatMl()} ml left",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = M3OnSurfaceVariant
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
                                            label = "ml",
                                            modifier = Modifier.width(72.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            autoCapitalize = false
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Row(
                                        modifier = Modifier.padding(start = 48.dp),
                                        horizontalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.sm)
                                    ) {
                                        UsageLipArea.entries.forEach { area ->
                                            val isAreaSelected = (entry?.lipArea ?: UsageLipArea.BOTH) == area
                                            FilledTonalButton(
                                                onClick = { viewModel.setBottleLipArea(bottle.id, area) },
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (isAreaSelected) M3PrimaryContainer else M3FieldBg,
                                                    contentColor = if (isAreaSelected) M3Primary else M3OnSurfaceVariant
                                                ),
                                                contentPadding = PaddingValues(horizontal = DasurvTheme.spacing.lg, vertical = DasurvTheme.spacing.sm)
                                            ) {
                                                Text(
                                                    when (area) {
                                                        UsageLipArea.UPPER -> "Upper"
                                                        UsageLipArea.LOWER -> "Lower"
                                                        UsageLipArea.BOTH -> "Both"
                                                    },
                                                    maxLines = 1
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

            if (costSummary.items.isNotEmpty()) {
                item {
                    CostBreakdown(costSummary = costSummary)
                }
            }

            // Save button
            item {
                Spacer(modifier = Modifier.height(DasurvTheme.spacing.sm))
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
                        viewModel.saveSession(
                            session = session,
                            equipmentList = equipment,
                            onSuccess = { sessionId ->
                                if (isTimerForThisClient) {
                                    timerViewModel.resetTimer()
                                }
                                onSessionCreated(sessionId)
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = M3Primary,
                        contentColor = Color.White,
                        disabledContainerColor = M3Primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(vertical = DasurvTheme.spacing.lg)
                ) {
                    Text("Create Session", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}
