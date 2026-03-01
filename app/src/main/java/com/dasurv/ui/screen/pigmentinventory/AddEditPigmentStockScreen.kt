package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatPrecise

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
            existingStock?.costPerUnit?.let { if (it > 0) it.formatCurrency() else "" } ?: ""
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
            CatalogCustomToggle(isCustom = isCustom, onSetCustom = { isCustom = it })
        }

        // Preview swatch
        PigmentPreviewSwatch(name = name, brand = brand, colorHex = colorHex)

        // Card 1: Catalog picker or Name+Brand
        DasurvFormCard {
            if (!isCustom && !isEditing) {
                PigmentCatalogDropdown(
                    selectedName = name,
                    selectedBrand = brand,
                    allPigments = allPigments,
                    onPigmentSelected = { pigment ->
                        name = pigment.name
                        brand = pigment.brand.displayName
                        colorHex = pigment.colorHex
                    }
                )
            } else {
                DasurvTextField(value = name, onValueChange = { name = it }, label = "Name *")
                DasurvTextField(value = brand, onValueChange = { brand = it }, label = "Brand")
            }
        }

        // Card 2: Cost/Bottle, Bottles in Stock
        DasurvFormCard {
            DasurvCurrencyField(
                value = costPerBottle,
                onValueChange = { costPerBottle = it },
                label = "Cost/Bottle"
            )
            DasurvTextField(
                value = stockCount,
                onValueChange = { stockCount = it },
                label = "Bottles in Stock",
                autoCapitalize = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (pricePerMl > 0) {
                Text(
                    "Cost per ml: \$${pricePerMl.formatPrecise()} (15ml bottle)",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3Primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = spacing.xs)
                )
            }
        }

        // Card 3: Notes
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
}
