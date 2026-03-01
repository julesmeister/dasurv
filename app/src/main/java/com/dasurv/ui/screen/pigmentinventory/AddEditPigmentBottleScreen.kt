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
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatMl
import com.dasurv.util.formatPrecise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPigmentBottleScreen(
    bottleId: Long?,
    onNavigateBack: () -> Unit,
    prefillName: String? = null,
    prefillBrand: String? = null,
    prefillColorHex: String? = null,
    viewModel: PigmentInventoryViewModel = hiltViewModel()
) {
    val isEditing = bottleId != null
    val existingBottle by viewModel.selectedBottle.collectAsStateWithLifecycle()
    val usageHistory by viewModel.usageHistory.collectAsStateWithLifecycle()
    val spacing = DasurvTheme.spacing

    LaunchedEffect(bottleId) {
        if (bottleId != null) viewModel.loadBottle(bottleId)
    }

    var isCustom by remember { mutableStateOf(prefillName == null) }
    var pigmentName by remember { mutableStateOf(prefillName ?: "") }
    var pigmentBrand by remember { mutableStateOf(prefillBrand ?: "") }
    var colorHex by remember { mutableStateOf(prefillColorHex ?: "#CCCCCC") }
    var bottleSizeText by remember { mutableStateOf("15") }
    var remainingMlText by remember { mutableStateOf("15") }
    var pricePerBottleText by remember { mutableStateOf("") }
    var pricePerMlText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val allPigments = viewModel.allPigments

    var priceEditSource by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(existingBottle) {
        if (existingBottle != null && !initialized) {
            val b = existingBottle!!
            isCustom = b.isCustom
            pigmentName = b.pigmentName
            pigmentBrand = b.pigmentBrand
            colorHex = b.colorHex
            bottleSizeText = if (b.bottleSizeMl == b.bottleSizeMl.toLong().toDouble())
                b.bottleSizeMl.toLong().toString() else b.bottleSizeMl.toString()
            remainingMlText = b.remainingMl.formatMl()
            pricePerBottleText = if (b.pricePerBottle > 0) b.pricePerBottle.formatCurrency() else ""
            pricePerMlText = if (b.pricePerMl > 0) b.pricePerMl.formatPrecise() else ""
            notes = b.notes
            initialized = true
        }
    }

    LaunchedEffect(pricePerBottleText, bottleSizeText) {
        if (priceEditSource == "bottle") {
            val price = pricePerBottleText.toDoubleOrNull()
            val size = bottleSizeText.toDoubleOrNull()
            if (price != null && size != null && size > 0) {
                pricePerMlText = (price / size).formatPrecise()
            }
            priceEditSource = null
        }
    }

    LaunchedEffect(pricePerMlText, bottleSizeText) {
        if (priceEditSource == "ml") {
            val ppm = pricePerMlText.toDoubleOrNull()
            val size = bottleSizeText.toDoubleOrNull()
            if (ppm != null && size != null && size > 0) {
                pricePerBottleText = (ppm * size).formatCurrency()
            }
            priceEditSource = null
        }
    }

    if (showDeleteDialog && existingBottle != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Delete Bottle",
            message = "Delete this pigment bottle and all its usage history?",
            onConfirm = { viewModel.deleteBottle(existingBottle!!) { onNavigateBack() } }
        )
    }

    DasurvFormScaffold(
        title = if (isEditing) "Edit Bottle" else "Add Bottle",
        onNavigateBack = onNavigateBack,
        actions = {
            if (isEditing) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = M3OnSurface)
                }
            }
        },
        saveText = if (isEditing) "Save Changes" else "Add Bottle",
        saveEnabled = pigmentName.isNotBlank() && pigmentBrand.isNotBlank(),
        snackbarMessage = if (isEditing) "Bottle updated" else "Bottle added",
        onSave = { onDone ->
            val bottle = PigmentBottle(
                id = existingBottle?.id ?: 0L,
                pigmentName = pigmentName.trim(),
                pigmentBrand = pigmentBrand.trim(),
                colorHex = colorHex.trim(),
                isCustom = isCustom,
                bottleSizeMl = bottleSizeText.toDoubleOrNull() ?: 15.0,
                remainingMl = remainingMlText.toDoubleOrNull() ?: 15.0,
                pricePerBottle = pricePerBottleText.toDoubleOrNull() ?: 0.0,
                pricePerMl = pricePerMlText.toDoubleOrNull() ?: 0.0,
                purchaseDate = existingBottle?.purchaseDate ?: System.currentTimeMillis(),
                notes = notes.trim()
            )
            viewModel.saveBottle(bottle) { onDone() }
        }
    ) {
        // Pigment source picker
        CatalogCustomToggle(isCustom = isCustom, onSetCustom = { isCustom = it })

        // Preview swatch
        PigmentPreviewSwatch(name = pigmentName, brand = pigmentBrand, colorHex = colorHex)

        // Card 1: Pigment source
        DasurvFormCard {
            if (!isCustom) {
                PigmentCatalogDropdown(
                    selectedName = pigmentName,
                    selectedBrand = pigmentBrand,
                    allPigments = allPigments,
                    onPigmentSelected = { pigment ->
                        pigmentName = pigment.name
                        pigmentBrand = pigment.brand.displayName
                        colorHex = pigment.colorHex
                    }
                )
            } else {
                DasurvTextField(value = pigmentName, onValueChange = { pigmentName = it }, label = "Name *")
                DasurvTextField(value = pigmentBrand, onValueChange = { pigmentBrand = it }, label = "Brand *")
                DasurvTextField(value = colorHex, onValueChange = { colorHex = it }, label = "Color Hex", autoCapitalize = false)
            }
        }

        // Card 2: Bottle Size, Remaining
        DasurvFormCard {
            DasurvTextField(
                value = bottleSizeText,
                onValueChange = { bottleSizeText = it },
                label = "Bottle Size (ml)",
                autoCapitalize = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            DasurvTextField(
                value = remainingMlText,
                onValueChange = { remainingMlText = it },
                label = "Remaining (ml)",
                autoCapitalize = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        // Card 3: Price/Bottle, Price/ml
        DasurvFormCard {
            DasurvCurrencyField(
                value = pricePerBottleText,
                onValueChange = {
                    pricePerBottleText = it
                    priceEditSource = "bottle"
                },
                label = "Price/Bottle"
            )
            DasurvCurrencyField(
                value = pricePerMlText,
                onValueChange = {
                    pricePerMlText = it
                    priceEditSource = "ml"
                },
                label = "Price/ml"
            )
        }

        // Card 4: Notes
        DasurvFormCard {
            DasurvTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                minLines = 2
            )
        }

        // Usage history (editing only)
        if (isEditing && usageHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                "Usage History",
                style = MaterialTheme.typography.titleMedium,
                color = M3OnSurface
            )
            M3ListCard {
                usageHistory.forEachIndexed { index, usage ->
                    UsageHistoryRow(usage)
                    if (index < usageHistory.lastIndex) {
                        M3ListDivider()
                    }
                }
            }
        }
    }
}
