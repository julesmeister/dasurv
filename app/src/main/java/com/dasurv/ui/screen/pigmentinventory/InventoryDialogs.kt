package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
                    label = { Text("Bottles to Add") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    val spacing = DasurvTheme.spacing

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Usage: ${bottle.pigmentName}", color = M3OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                Text(
                    "Remaining: ${String.format("%.1f", bottle.remainingMl)} ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )

                // Client picker
                ExposedDropdownMenuBox(
                    expanded = clientDropdownExpanded,
                    onExpandedChange = { clientDropdownExpanded = it }
                ) {
                    DasurvTextField(
                        value = clients.find { it.id == selectedClientId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Client") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false },
                        scrollState = rememberScrollState(),
                        shadowElevation = 0.dp
                    ) {
                        clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client.name, color = M3OnSurface) },
                                onClick = {
                                    selectedClientId = client.id
                                    clientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Lip area
                Text("Lip Area", style = MaterialTheme.typography.labelMedium, color = M3OnSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    UsageLipArea.entries.forEach { area ->
                        FilterChip(
                            selected = selectedLipArea == area,
                            onClick = { selectedLipArea = area },
                            label = {
                                Text(
                                    when (area) {
                                        UsageLipArea.UPPER -> "Upper"
                                        UsageLipArea.LOWER -> "Lower"
                                        UsageLipArea.BOTH -> "Both"
                                    }
                                )
                            }
                        )
                    }
                }

                // ml used
                DasurvTextField(
                    value = mlUsedText,
                    onValueChange = { mlUsedText = it },
                    label = { Text("ml Used") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                DasurvTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
