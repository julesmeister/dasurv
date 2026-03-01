package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPigmentStockScreen(
    equipmentId: Long?,
    onNavigateBack: () -> Unit,
    prefillName: String? = null,
    prefillBrand: String? = null,
    prefillColorHex: String? = null,
    viewModel: PigmentInventoryViewModel = hiltViewModel()
) {
    val isEditing = equipmentId != null && equipmentId != 0L
    val existingStock by viewModel.selectedStock.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    LaunchedEffect(equipmentId) {
        if (isEditing) viewModel.loadStock(equipmentId!!)
    }

    val allPigments = viewModel.allPigments
    var catalogExpanded by remember { mutableStateOf(false) }
    var isCustom by remember { mutableStateOf(prefillName == null && !isEditing) }

    var name by remember(existingStock) {
        mutableStateOf(existingStock?.name ?: prefillName ?: "")
    }
    var brand by remember(existingStock) {
        mutableStateOf(existingStock?.brand ?: prefillBrand ?: "")
    }
    var colorHex by remember { mutableStateOf(prefillColorHex ?: "#CCCCCC") }
    var costPerBottle by remember(existingStock) {
        mutableStateOf(
            existingStock?.costPerUnit?.let { if (it > 0) String.format("%.2f", it) else "" } ?: ""
        )
    }
    var stockCount by remember(existingStock) {
        mutableStateOf(existingStock?.stockQuantity?.toString() ?: "1")
    }
    var notes by remember(existingStock) {
        mutableStateOf(existingStock?.notes ?: "")
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(existingStock, name) {
        if (existingStock != null) {
            val catalogPigment = allPigments.find { it.name == existingStock!!.name }
            if (catalogPigment != null) {
                colorHex = catalogPigment.colorHex
                isCustom = false
            } else {
                isCustom = true
            }
        }
    }

    val pricePerMl = remember(costPerBottle) {
        val cost = costPerBottle.toDoubleOrNull() ?: 0.0
        if (cost > 0) cost / 15.0 else 0.0
    }

    if (showDeleteDialog && existingStock != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Delete Pigment Stock",
            message = "Delete ${existingStock!!.name} and remove it from your inventory?",
            onConfirm = { viewModel.deleteStock(existingStock!!) { onNavigateBack() } }
        )
    }

    DasurvFormScaffold(
        title = if (isEditing) "Edit Pigment" else "Add Pigment",
        onNavigateBack = onNavigateBack,
        actions = {
            if (isEditing) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = M3OnSurface)
                }
            }
        },
        saveText = if (isEditing) "Save Changes" else "Add Pigment",
        saveEnabled = name.isNotBlank(),
        snackbarMessage = if (isEditing) "Pigment updated" else "Pigment added",
        onSave = { onDone ->
            val equipment = Equipment(
                id = if (isEditing) equipmentId!! else 0,
                name = name.trim(),
                category = "pigment",
                brand = brand.trim(),
                costPerUnit = costPerBottle.toDoubleOrNull() ?: 0.0,
                stockQuantity = stockCount.toIntOrNull() ?: 0,
                type = "consumable",
                piecesPerPackage = 1,
                notes = notes.trim()
            )
            viewModel.saveStock(equipment) { onDone() }
        }
    ) {
        // Catalog/Custom toggle (add mode only)
        if (!isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                FilterChip(
                    selected = !isCustom,
                    onClick = { isCustom = false },
                    label = { Text("From Catalog") }
                )
                FilterChip(
                    selected = isCustom,
                    onClick = { isCustom = true },
                    label = { Text("Custom") }
                )
            }
        }

        // Preview swatch
        if (name.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorSwatch(colorHex = colorHex, label = "")
                Spacer(modifier = Modifier.width(spacing.md))
                Column {
                    Text(name, style = MaterialTheme.typography.titleSmall, color = M3OnSurface)
                    Text(
                        brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
            }
        }

        // Card 1: Catalog picker or Name+Brand
        DasurvFormCard {
            if (!isCustom && !isEditing) {
                // Catalog picker
                ExposedDropdownMenuBox(
                    expanded = catalogExpanded,
                    onExpandedChange = { catalogExpanded = it }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pigment",
                            style = FormDefaults.LabelStyle,
                            modifier = Modifier.width(FormDefaults.LabelWidth)
                        )
                        Text(
                            text = if (name.isNotBlank()) "$name ($brand)" else "",
                            style = FormDefaults.ValueStyle,
                            modifier = Modifier.weight(1f)
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = catalogExpanded)
                    }
                    ExposedDropdownMenu(
                        expanded = catalogExpanded,
                        onDismissRequest = { catalogExpanded = false },
                        scrollState = rememberScrollState(),
                        shadowElevation = 0.dp
                    ) {
                        allPigments.forEach { pigment ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ColorSwatch(colorHex = pigment.colorHex, label = "")
                                        Spacer(modifier = Modifier.width(spacing.sm))
                                        Column {
                                            Text(pigment.name, color = M3OnSurface)
                                            Text(
                                                pigment.brand.displayName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = M3OnSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    name = pigment.name
                                    brand = pigment.brand.displayName
                                    colorHex = pigment.colorHex
                                    catalogExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                FormRow(label = "Name *", value = name, onValueChange = { name = it })
                FormRow(label = "Brand", value = brand, onValueChange = { brand = it })
            }
        }

        // Card 2: Cost/Bottle, Bottles in Stock
        DasurvFormCard {
            FormRow(
                label = "Cost/Bottle",
                value = costPerBottle,
                onValueChange = { costPerBottle = it },
                keyboardType = KeyboardType.Decimal
            )
            FormRow(
                label = "Bottles in Stock",
                value = stockCount,
                onValueChange = { stockCount = it },
                keyboardType = KeyboardType.Number
            )
            if (pricePerMl > 0) {
                Text(
                    "Cost per ml: \$${String.format("%.4f", pricePerMl)} (15ml bottle)",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = InterFontFamily),
                    color = M3Primary,
                    modifier = Modifier.padding(start = FormDefaults.LabelWidth, bottom = spacing.xs)
                )
            }
        }

        // Card 3: Notes
        DasurvFormCard {
            FormRow(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it },
                singleLine = false
            )
        }
    }
}
