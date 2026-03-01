package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary

@Composable
internal fun EquipmentLogUsageDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var quantity by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Usage: ${equipment.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Stock: ${equipment.stockQuantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
                DasurvTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity Used") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                DasurvTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantity.toDoubleOrNull()
                    if (qty != null && qty > 0) {
                        onConfirm(qty, notes.trim())
                    }
                },
                enabled = (quantity.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("Log", color = M3Primary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
