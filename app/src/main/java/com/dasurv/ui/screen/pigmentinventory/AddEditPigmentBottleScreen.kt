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
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.ui.component.*

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
    var catalogExpanded by remember { mutableStateOf(false) }

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
            remainingMlText = String.format("%.1f", b.remainingMl)
            pricePerBottleText = if (b.pricePerBottle > 0) String.format("%.2f", b.pricePerBottle) else ""
            pricePerMlText = if (b.pricePerMl > 0) String.format("%.4f", b.pricePerMl) else ""
            notes = b.notes
            initialized = true
        }
    }

    LaunchedEffect(pricePerBottleText, bottleSizeText) {
        if (priceEditSource == "bottle") {
            val price = pricePerBottleText.toDoubleOrNull()
            val size = bottleSizeText.toDoubleOrNull()
            if (price != null && size != null && size > 0) {
                pricePerMlText = String.format("%.4f", price / size)
            }
            priceEditSource = null
        }
    }

    LaunchedEffect(pricePerMlText, bottleSizeText) {
        if (priceEditSource == "ml") {
            val ppm = pricePerMlText.toDoubleOrNull()
            val size = bottleSizeText.toDoubleOrNull()
            if (ppm != null && size != null && size > 0) {
                pricePerBottleText = String.format("%.2f", ppm * size)
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
                    Icon(Icons.Default.Delete, "Delete")
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
        // Pigment source picker (above cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        // Preview swatch
        if (pigmentName.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ColorSwatch(colorHex = colorHex, label = "")
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(pigmentName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        pigmentBrand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Card 1: Pigment source
        DasurvFormCard {
            if (!isCustom) {
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
                            text = if (pigmentName.isNotBlank()) "$pigmentName ($pigmentBrand)" else "",
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
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(pigment.name)
                                            Text(
                                                pigment.brand.displayName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    pigmentName = pigment.name
                                    pigmentBrand = pigment.brand.displayName
                                    colorHex = pigment.colorHex
                                    catalogExpanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                FormRow(label = "Name *", value = pigmentName, onValueChange = { pigmentName = it })
                FormRow(label = "Brand *", value = pigmentBrand, onValueChange = { pigmentBrand = it })
                FormRow(label = "Color Hex", value = colorHex, onValueChange = { colorHex = it })
            }
        }

        // Card 2: Bottle Size, Remaining
        DasurvFormCard {
            FormRow(
                label = "Bottle Size (ml)",
                value = bottleSizeText,
                onValueChange = { bottleSizeText = it },
                keyboardType = KeyboardType.Decimal
            )
            FormRow(
                label = "Remaining (ml)",
                value = remainingMlText,
                onValueChange = { remainingMlText = it },
                keyboardType = KeyboardType.Decimal
            )
        }

        // Card 3: Price/Bottle, Price/ml
        DasurvFormCard {
            FormRow(
                label = "Price/Bottle",
                value = pricePerBottleText,
                onValueChange = {
                    pricePerBottleText = it
                    priceEditSource = "bottle"
                },
                keyboardType = KeyboardType.Decimal
            )
            FormRow(
                label = "Price/ml",
                value = pricePerMlText,
                onValueChange = {
                    pricePerMlText = it
                    priceEditSource = "ml"
                },
                keyboardType = KeyboardType.Decimal
            )
        }

        // Card 4: Notes
        DasurvFormCard {
            FormRow(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it },
                singleLine = false
            )
        }

        // Usage history (editing only)
        if (isEditing && usageHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Usage History", style = MaterialTheme.typography.titleMedium)
            usageHistory.forEach { usage ->
                UsageHistoryRow(usage)
            }
        }
    }
}
