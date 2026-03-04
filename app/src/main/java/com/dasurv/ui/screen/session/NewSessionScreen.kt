package com.dasurv.ui.screen.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Session
import com.dasurv.ui.component.DasurvDropdownField
import com.dasurv.ui.component.DasurvMultiPageDialog
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.theme.DasurvTheme

@Composable
fun NewSessionDialog(
    clientId: Long,
    onDismiss: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val equipment by viewModel.allEquipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedIds by viewModel.selectedEquipmentIds.collectAsStateWithLifecycle()
    val quantities by viewModel.equipmentQuantities.collectAsStateWithLifecycle()
    val bottles by viewModel.allBottles.collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedBottleIds by viewModel.selectedBottleIds.collectAsStateWithLifecycle()
    val bottleEntries by viewModel.bottleEntries.collectAsStateWithLifecycle()

    val templates by viewModel.allTemplates.collectAsStateWithLifecycle()
    val consumables = remember(equipment) { equipment.filter { it.type == "consumable" } }

    var procedure by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var lipColorCategory by remember { mutableStateOf("") }
    var lipColorHex by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }

    val costSummary = remember(selectedIds, equipment, quantities, selectedBottleIds, bottles, bottleEntries) {
        viewModel.calculateCost(equipment, bottles)
    }

    DasurvMultiPageDialog(
        title = { page ->
            when (page) {
                0 -> "Session Details"
                1 -> "Consumables"
                else -> "Pigments & Cost"
            }
        },
        icon = { page ->
            when (page) {
                0 -> Icons.AutoMirrored.Filled.EventNote
                1 -> Icons.Default.Healing
                else -> Icons.Default.Palette
            }
        },
        pageCount = 3,
        currentPage = currentPage,
        onPageChange = { currentPage = it },
        onDismiss = onDismiss,
        isSaving = isSaving,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val session = Session(
                    clientId = clientId,
                    procedure = procedure.trim(),
                    notes = notes.trim(),
                    lipColorCategory = lipColorCategory.trim().ifBlank { null },
                    lipColorHex = lipColorHex.trim().ifBlank { null },
                    totalCost = costSummary.totalCost,
                    durationSeconds = 0,
                    upperLipSeconds = 0,
                    lowerLipSeconds = 0
                )
                viewModel.saveSession(
                    session = session,
                    equipmentList = equipment,
                    onSuccess = { onDismiss() }
                )
            }
        },
    ) { page ->
        when (page) {
            0 -> SessionDetailsPage(
                procedure = procedure,
                onProcedureChange = { procedure = it },
                lipColorCategory = lipColorCategory,
                onLipColorCategoryChange = { lipColorCategory = it },
                lipColorHex = lipColorHex,
                onLipColorHexChange = { lipColorHex = it },
                notes = notes,
                onNotesChange = { notes = it },
            )
            1 -> ConsumablesPage(
                consumables = consumables,
                selectedIds = selectedIds,
                quantities = quantities,
                onToggle = viewModel::toggleEquipment,
                onSetQuantity = { id, qty -> viewModel.setEquipmentQuantity(id, qty.toDouble()) },
                templates = templates,
                onLoadTemplate = { template ->
                    viewModel.loadTemplate(template) { loadedProcedure ->
                        if (procedure.isBlank() && loadedProcedure.isNotBlank()) {
                            procedure = loadedProcedure
                        }
                    }
                },
            )
            2 -> PigmentsCostPage(
                bottles = bottles,
                selectedBottleIds = selectedBottleIds,
                bottleEntries = bottleEntries,
                onToggleBottle = viewModel::toggleBottle,
                onSetMlUsed = viewModel::setBottleMlUsed,
                onSetLipArea = viewModel::setBottleLipArea,
                costSummary = costSummary,
            )
        }
    }
}

@Composable
private fun SessionDetailsPage(
    procedure: String,
    onProcedureChange: (String) -> Unit,
    lipColorCategory: String,
    onLipColorCategoryChange: (String) -> Unit,
    lipColorHex: String,
    onLipColorHexChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DasurvTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.md),
    ) {
        DasurvDropdownField(
            value = procedure,
            label = "Procedure",
            options = com.dasurv.util.PROCEDURE_TYPES,
            onOptionSelected = onProcedureChange,
        )
        DasurvTextField(
            value = lipColorCategory,
            onValueChange = onLipColorCategoryChange,
            label = "Lip Category",
        )
        DasurvTextField(
            value = lipColorHex,
            onValueChange = onLipColorHexChange,
            label = "Color Hex",
            autoCapitalize = false,
        )
        DasurvTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Notes",
            singleLine = false,
            minLines = 3,
        )
    }
}
