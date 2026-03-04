package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatPrecise

@Composable
fun EquipmentFormDialog(
    equipmentId: Long?,
    onDismiss: () -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val existingEquipment by viewModel.selectedEquipment.collectAsStateWithLifecycle()

    LaunchedEffect(equipmentId) {
        if (equipmentId != null && equipmentId != 0L) {
            viewModel.loadEquipment(equipmentId)
        } else {
            viewModel.clearSelectedEquipment()
        }
    }

    var name by remember(existingEquipment) { mutableStateOf(existingEquipment?.name ?: "") }
    var category by remember(existingEquipment) { mutableStateOf(existingEquipment?.category ?: "") }
    var brand by remember(existingEquipment) { mutableStateOf(existingEquipment?.brand ?: "") }
    var type by remember(existingEquipment) { mutableStateOf(existingEquipment?.type ?: "consumable") }
    var costPerUnit by remember(existingEquipment) {
        mutableStateOf(existingEquipment?.costPerUnit?.let { if (it > 0) it.toString() else "" } ?: "")
    }
    var piecesPerPackage by remember(existingEquipment) {
        mutableStateOf(existingEquipment?.piecesPerPackage?.toString() ?: "1")
    }
    var unitsPerSession by remember(existingEquipment) {
        mutableStateOf(existingEquipment?.unitsPerSession?.toString() ?: "1")
    }
    var stockQuantity by remember(existingEquipment) {
        mutableStateOf(existingEquipment?.stockQuantity?.toString() ?: "0")
    }
    var notes by remember(existingEquipment) { mutableStateOf(existingEquipment?.notes ?: "") }
    var purchaseSource by remember(existingEquipment) { mutableStateOf(existingEquipment?.purchaseSource ?: "") }
    var seller by remember(existingEquipment) { mutableStateOf(existingEquipment?.seller ?: "") }
    var minStockThreshold by remember(existingEquipment) {
        mutableStateOf(existingEquipment?.minStockThreshold?.let { if (it > 0) it.toString() else "" } ?: "")
    }

    val purchaseSources by viewModel.purchaseSources.collectAsStateWithLifecycle(initialValue = emptyList())
    val sellers by viewModel.sellers.collectAsStateWithLifecycle(initialValue = emptyList())

    val isEditing = equipmentId != null && equipmentId != 0L
    val categories = com.dasurv.util.EQUIPMENT_CATEGORIES
    val types = com.dasurv.util.EQUIPMENT_TYPES
    val isConsumable = type == "consumable"

    val costPerPiece = remember(costPerUnit, piecesPerPackage) {
        val cost = costPerUnit.toDoubleOrNull() ?: 0.0
        val pieces = piecesPerPackage.toIntOrNull() ?: 1
        if (pieces > 0) cost / pieces else 0.0
    }

    var isSaving by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }

    DasurvMultiPageDialog(
        title = { page ->
            when (page) {
                0 -> if (isEditing) "Edit Equipment" else "Equipment Details"
                else -> "Cost & Stock"
            }
        },
        icon = { page ->
            when (page) {
                0 -> Icons.Default.Build
                else -> Icons.Default.Inventory2
            }
        },
        pageCount = 2,
        currentPage = currentPage,
        onPageChange = { currentPage = it },
        onDismiss = onDismiss,
        isEdit = isEditing,
        confirmEnabled = name.isNotBlank(),
        isSaving = isSaving,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val equipment = Equipment(
                    id = if (isEditing) equipmentId!! else 0,
                    name = name.trim(),
                    category = category,
                    brand = brand.trim(),
                    costPerUnit = costPerUnit.toDoubleOrNull() ?: 0.0,
                    unitsPerSession = unitsPerSession.toDoubleOrNull() ?: 1.0,
                    stockQuantity = stockQuantity.toIntOrNull() ?: 0,
                    notes = notes.trim(),
                    type = type,
                    piecesPerPackage = piecesPerPackage.toIntOrNull() ?: 1,
                    purchaseSource = purchaseSource.trim(),
                    seller = seller.trim(),
                    minStockThreshold = minStockThreshold.toIntOrNull() ?: 0
                )
                viewModel.saveEquipment(equipment) { onDismiss() }
            }
        },
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when (page) {
                0 -> {
                    // Page 0: Equipment Details
                    DasurvDropdownField(
                        value = type.replaceFirstChar { it.uppercase() },
                        label = "Type",
                        options = types,
                        onOptionSelected = { type = it.lowercase() }
                    )
                    DasurvDropdownField(
                        value = category.replaceFirstChar { it.uppercase() }.ifEmpty { "" },
                        label = "Category",
                        options = categories,
                        onOptionSelected = { category = it.lowercase() }
                    )
                    DasurvTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Name *"
                    )
                    DasurvTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "Brand"
                    )
                    DasurvAutoCompleteField(
                        value = purchaseSource,
                        onValueChange = { purchaseSource = it },
                        label = "Purchased From (App/Platform)",
                        suggestions = purchaseSources,
                        placeholder = "e.g. Shopee, Lazada"
                    )
                    DasurvAutoCompleteField(
                        value = seller,
                        onValueChange = { seller = it },
                        label = "Seller",
                        suggestions = sellers,
                        placeholder = "e.g. Store name"
                    )
                }
                1 -> {
                    // Page 1: Cost & Stock
                    if (isConsumable) {
                        DasurvCurrencyField(
                            value = costPerUnit,
                            onValueChange = { costPerUnit = it },
                            label = "Package Cost"
                        )
                        DasurvTextField(
                            value = piecesPerPackage,
                            onValueChange = { piecesPerPackage = it },
                            label = "Pieces per Package",
                            autoCapitalize = false,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        if (costPerPiece > 0) {
                            Text(
                                "Cost per piece: ₱${costPerPiece.formatPrecise()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = M3Primary,
                                modifier = Modifier.padding(
                                    start = 4.dp,
                                    bottom = DasurvTheme.spacing.xs
                                )
                            )
                        }
                    } else {
                        DasurvCurrencyField(
                            value = costPerUnit,
                            onValueChange = { costPerUnit = it },
                            label = "Cost"
                        )
                    }
                    DasurvTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = if (isConsumable) "Stock (pcs)" else "Stock Qty",
                        autoCapitalize = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    DasurvTextField(
                        value = unitsPerSession,
                        onValueChange = { unitsPerSession = it },
                        label = "Units per Session",
                        autoCapitalize = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    DasurvTextField(
                        value = minStockThreshold,
                        onValueChange = { minStockThreshold = it },
                        label = "Low Stock Alert Threshold",
                        autoCapitalize = false,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    DasurvTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes",
                        singleLine = false,
                        minLines = 2
                    )
                }
            }
        }
    }
}
