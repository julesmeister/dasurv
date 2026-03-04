package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatMl

@Composable
internal fun RestockDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var countText by remember { mutableStateOf("1") }
    val spacing = DasurvTheme.spacing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock ${equipment.name}", color = M3OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    "Current stock: ${equipment.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
                DasurvTextField(
                    value = countText,
                    onValueChange = { countText = it },
                    label = "Bottles to Add",
                    autoCapitalize = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val count = countText.toIntOrNull()
                    if (count != null && count > 0) onConfirm(count)
                },
                enabled = (countText.toIntOrNull() ?: 0) > 0
            ) { Text("Add", color = M3Primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = M3OnSurfaceVariant) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogUsageDialog(
    bottle: PigmentBottle,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onConfirm: (clientId: Long, lipArea: UsageLipArea, mlUsed: Double, notes: String) -> Unit
) {
    var selectedClientId by remember { mutableStateOf<Long?>(null) }
    var selectedLipArea by remember { mutableStateOf(UsageLipArea.BOTH) }
    var mlUsedText by remember { mutableStateOf("0.5") }
    var notes by remember { mutableStateOf("") }
    val spacing = DasurvTheme.spacing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Usage: ${bottle.pigmentName}", color = M3OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Text(
                    "Remaining: ${bottle.remainingMl.formatMl()} ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )

                // Client picker
                DasurvDropdownField(
                    value = clients.find { it.id == selectedClientId }?.name ?: "",
                    label = "Client",
                    options = clients.map { it.name },
                    onOptionSelected = { name ->
                        clients.find { it.name == name }?.let { selectedClientId = it.id }
                    }
                )

                // Lip area
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    UsageLipArea.entries.forEach { area ->
                        val isSelected = selectedLipArea == area
                        DasurvFilterChip(
                            label = when (area) {
                                UsageLipArea.UPPER -> "Upper"
                                UsageLipArea.LOWER -> "Lower"
                                UsageLipArea.BOTH -> "Both"
                            },
                            selected = isSelected,
                            onClick = { selectedLipArea = area }
                        )
                    }
                }

                // ml used
                DasurvTextField(
                    value = mlUsedText,
                    onValueChange = { mlUsedText = it },
                    label = "ml Used",
                    autoCapitalize = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Notes
                DasurvTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val ml = mlUsedText.toDoubleOrNull() ?: return@TextButton
                    val clientId = selectedClientId ?: return@TextButton
                    onConfirm(clientId, selectedLipArea, ml, notes)
                },
                enabled = selectedClientId != null && (mlUsedText.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                Text("Log", color = M3Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = M3OnSurfaceVariant) }
        }
    )
}
