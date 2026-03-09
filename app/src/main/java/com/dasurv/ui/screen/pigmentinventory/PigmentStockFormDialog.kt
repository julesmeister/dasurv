package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.model.PigmentBrand
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatPrecise

@Composable
fun PigmentStockFormDialog(
    equipmentId: Long?,
    prefillName: String? = null,
    prefillBrand: String? = null,
    prefillColorHex: String? = null,
    onDismiss: () -> Unit,
    viewModel: PigmentInventoryViewModel
) {
    val isEditing = equipmentId != null && equipmentId != 0L
    val existingStock: Equipment? by viewModel.selectedStock.collectAsStateWithLifecycle()

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
        val cost = existingStock?.costPerUnit ?: 0.0
        mutableStateOf(if (cost > 0) cost.formatCurrency() else "")
    }
    var stockCount by remember(existingStock) {
        mutableStateOf(existingStock?.stockQuantity?.toString() ?: "1")
    }
    var notes by remember(existingStock) {
        mutableStateOf(existingStock?.notes ?: "")
    }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(existingStock, name) {
        val stock = existingStock ?: return@LaunchedEffect
        val catalogPigment = allPigments.find { it.name == stock.name }
        if (catalogPigment != null) {
            colorHex = catalogPigment.colorHex
            isCustom = false
        } else {
            isCustom = true
        }
    }

    val pricePerMl = remember(costPerBottle) {
        val cost = costPerBottle.toDoubleOrNull() ?: 0.0
        if (cost > 0) cost / 15.0 else 0.0
    }

    val useCatalogPicker = !isCustom && !isEditing && prefillName == null

    if (useCatalogPicker) {
        // Multi-page: Page 1 = pick from catalog grid, Page 2 = cost/stock
        var currentPage by remember { mutableIntStateOf(0) }
        var selectedBrandFilter by remember { mutableStateOf<PigmentBrand?>(null) }

        val filteredPigments = remember(selectedBrandFilter, allPigments) {
            if (selectedBrandFilter == null) allPigments
            else allPigments.filter { it.brand == selectedBrandFilter }
        }

        DasurvMultiPageDialog(
            title = { page -> if (page == 0) "Select Pigment" else "Cost & Stock" },
            icon = { page -> if (page == 0) Icons.Default.Palette else Icons.Default.Inventory2 },
            pageCount = 2,
            currentPage = currentPage,
            onPageChange = { currentPage = it },
            onDismiss = onDismiss,
            confirmEnabled = name.isNotBlank() && !isSaving,
            isSaving = isSaving,
            onConfirm = {
                if (!isSaving) {
                    isSaving = true
                    val equipment = buildEquipment(
                        isEditing, equipmentId, name, brand, costPerBottle, stockCount, notes
                    )
                    viewModel.saveStock(equipment) { onDismiss() }
                }
            },
            headerExtra = if (currentPage == 0 && name.isNotBlank()) {
                { PigmentSelectedBadge(name = name, brand = brand, colorHex = colorHex) }
            } else null,
        ) { page ->
            when (page) {
                0 -> {
                    // Catalog picker page
                    val spacing = DasurvTheme.spacing
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Brand filter chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = spacing.lg, vertical = spacing.sm),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            val allBrands = listOf<PigmentBrand?>(null) + PigmentBrand.entries
                            allBrands.forEach { brandFilter ->
                                val isSelected = selectedBrandFilter == brandFilter
                                FilledTonalButton(
                                    onClick = { selectedBrandFilter = brandFilter },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isSelected) M3PrimaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh,
                                        contentColor = if (isSelected) M3Primary
                                        else M3OnSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = spacing.lg,
                                        vertical = spacing.sm
                                    )
                                ) {
                                    Text(brandFilter?.displayName ?: "All", maxLines = 1)
                                }
                            }
                        }

                        Text(
                            "${filteredPigments.size} colors",
                            modifier = Modifier.padding(horizontal = spacing.lg),
                            style = MaterialTheme.typography.bodySmall,
                            color = M3OnSurfaceVariant
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredPigments) { pigment ->
                                ColorSwatch(
                                    colorHex = pigment.colorHex,
                                    label = pigment.name,
                                    selected = pigment.name == name,
                                    onClick = {
                                        name = pigment.name
                                        brand = pigment.brand.displayName
                                        colorHex = pigment.colorHex
                                    }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Cost & stock page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        PigmentPreviewSwatch(name = name, brand = brand, colorHex = colorHex)
                        PigmentCostStockFields(
                            costPerBottle = costPerBottle,
                            onCostChange = { costPerBottle = it },
                            stockCount = stockCount,
                            onStockChange = { stockCount = it },
                            pricePerMl = pricePerMl,
                            notes = notes,
                            onNotesChange = { notes = it }
                        )
                    }
                }
            }
        }
    } else {
        // Single-page form: custom add, editing, or prefilled from catalog screen
        DasurvFormDialog(
            title = if (isEditing) "Edit Pigment" else "Add Pigment",
            icon = Icons.Default.Palette,
            onDismiss = onDismiss,
            confirmLabel = if (isEditing) "Save" else "Add",
            confirmEnabled = name.isNotBlank() && !isSaving,
            isLoading = isSaving,
            onDelete = if (isEditing) {
                {
                    val stock = existingStock ?: return@DasurvFormDialog
                    viewModel.deleteStock(stock) { onDismiss() }
                }
            } else null,
            onConfirm = {
                if (!isSaving) {
                    isSaving = true
                    val equipment = buildEquipment(
                        isEditing, equipmentId, name, brand, costPerBottle, stockCount, notes
                    )
                    viewModel.saveStock(equipment) { onDismiss() }
                }
            }
        ) {
            if (!isEditing) {
                CatalogCustomToggle(isCustom = isCustom, onSetCustom = { isCustom = it })
            }
            PigmentPreviewSwatch(name = name, brand = brand, colorHex = colorHex)
            DasurvTextField(value = name, onValueChange = { name = it }, label = "Name *")
            DasurvTextField(value = brand, onValueChange = { brand = it }, label = "Brand")
            PigmentCostStockFields(
                costPerBottle = costPerBottle,
                onCostChange = { costPerBottle = it },
                stockCount = stockCount,
                onStockChange = { stockCount = it },
                pricePerMl = pricePerMl,
                notes = notes,
                onNotesChange = { notes = it }
            )
        }
    }
}

@Composable
private fun PigmentSelectedBadge(name: String, brand: String, colorHex: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        ColorSwatch(colorHex = colorHex, label = "")
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "$name · $brand",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = M3OnSurfaceVariant
        )
    }
}

@Composable
private fun PigmentCostStockFields(
    costPerBottle: String,
    onCostChange: (String) -> Unit,
    stockCount: String,
    onStockChange: (String) -> Unit,
    pricePerMl: Double,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    DasurvCurrencyField(
        value = costPerBottle,
        onValueChange = onCostChange,
        label = "Cost/Bottle"
    )
    DasurvTextField(
        value = stockCount,
        onValueChange = onStockChange,
        label = "Bottles in Stock",
        autoCapitalize = false,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
    if (pricePerMl > 0) {
        Text(
            "Cost per ml: ₱${pricePerMl.formatPrecise()} (15ml bottle)",
            style = MaterialTheme.typography.bodySmall,
            color = M3Primary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
    DasurvTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = "Notes",
        singleLine = false,
        minLines = 2
    )
}

private fun buildEquipment(
    isEditing: Boolean,
    equipmentId: Long?,
    name: String,
    brand: String,
    costPerBottle: String,
    stockCount: String,
    notes: String
) = Equipment(
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
