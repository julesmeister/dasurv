package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.layout.*
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

@Composable
fun AddEditEquipmentScreen(
    equipmentId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val existingEquipment by viewModel.selectedEquipment.collectAsStateWithLifecycle()

    LaunchedEffect(equipmentId) {
        if (equipmentId != null && equipmentId != 0L) {
            viewModel.loadEquipment(equipmentId)
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

    val isEditing = equipmentId != null && equipmentId != 0L
    val categories = listOf("Needle", "Numbing", "Aftercare", "Other")
    val types = listOf("Consumable", "Studio")
    val isConsumable = type == "consumable"

    val costPerPiece = remember(costPerUnit, piecesPerPackage) {
        val cost = costPerUnit.toDoubleOrNull() ?: 0.0
        val pieces = piecesPerPackage.toIntOrNull() ?: 1
        if (pieces > 0) cost / pieces else 0.0
    }

    DasurvFormScaffold(
        title = if (isEditing) "Edit Equipment" else "Add Equipment",
        onNavigateBack = onNavigateBack,
        saveText = if (isEditing) "Update" else "Save",
        saveEnabled = name.isNotBlank(),
        snackbarMessage = if (isEditing) "Equipment updated" else "Equipment saved",
        onSave = { onDone ->
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
                piecesPerPackage = piecesPerPackage.toIntOrNull() ?: 1
            )
            viewModel.saveEquipment(equipment) { onDone() }
        }
    ) {
        // Card 1: Type, Category, Name, Brand
        DasurvFormCard {
            FormDropdownRow(
                label = "Type",
                value = type.replaceFirstChar { it.uppercase() },
                options = types,
                onOptionSelected = { type = it.lowercase() }
            )
            FormDropdownRow(
                label = "Category",
                value = category.replaceFirstChar { it.uppercase() }.ifEmpty { "" },
                options = categories,
                onOptionSelected = { category = it.lowercase() }
            )
            FormRow(label = "Name *", value = name, onValueChange = { name = it })
            FormRow(label = "Brand", value = brand, onValueChange = { brand = it })
        }

        // Card 2: Cost fields, Stock, Units/Session
        DasurvFormCard {
            if (isConsumable) {
                FormRow(
                    label = "Package Cost",
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    keyboardType = KeyboardType.Decimal
                )
                FormRow(
                    label = "Pcs/Package",
                    value = piecesPerPackage,
                    onValueChange = { piecesPerPackage = it },
                    keyboardType = KeyboardType.Number
                )
                if (costPerPiece > 0) {
                    Text(
                        "Cost per piece: \$${String.format("%.4f", costPerPiece)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = InterFontFamily),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = FormDefaults.LabelWidth, bottom = 4.dp)
                    )
                }
            } else {
                FormRow(
                    label = "Cost",
                    value = costPerUnit,
                    onValueChange = { costPerUnit = it },
                    keyboardType = KeyboardType.Decimal
                )
            }
            FormRow(
                label = if (isConsumable) "Stock (pcs)" else "Stock Qty",
                value = stockQuantity,
                onValueChange = { stockQuantity = it },
                keyboardType = KeyboardType.Number
            )
            FormRow(
                label = "Units/Session",
                value = unitsPerSession,
                onValueChange = { unitsPerSession = it },
                keyboardType = KeyboardType.Decimal
            )
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
