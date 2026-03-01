package com.dasurv.ui.screen.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.DasurvTextField

internal fun LazyListScope.consumableItems(
    consumables: List<Equipment>,
    selectedIds: Set<Long>,
    quantities: Map<Long, Double>,
    onToggleEquipment: (Long) -> Unit,
    onSetQuantity: (Long, Double) -> Unit
) {
    if (consumables.isEmpty()) return

    item {
        Spacer(modifier = Modifier.height(4.dp))
        Text("Consumables Used", style = MaterialTheme.typography.titleMedium)
    }

    items(consumables, key = { it.id }) { item ->
        val isSelected = item.id in selectedIds
        val qty = quantities[item.id] ?: 1.0

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleEquipment(item.id) }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$${String.format("%.4f", item.costPerPiece)} / piece" +
                        if (item.piecesPerPackage > 1)
                            " (${item.piecesPerPackage}/pkg)"
                        else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                var qtyText by remember(item.id, qty) {
                    mutableStateOf(if (qty == qty.toLong().toDouble()) qty.toLong().toString() else qty.toString())
                }
                DasurvTextField(
                    value = qtyText,
                    onValueChange = { newVal ->
                        qtyText = newVal
                        newVal.toDoubleOrNull()?.let { onSetQuantity(item.id, it) }
                    },
                    label = { Text("Qty") },
                    modifier = Modifier.width(72.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        }
    }
}

internal fun LazyListScope.pigmentBottleItems(
    bottles: List<PigmentBottle>,
    selectedBottleIds: Set<Long>,
    bottleEntries: Map<Long, @JvmSuppressWildcards Any>,
    onToggleBottle: (Long) -> Unit,
    onSetMlUsed: (Long, Double) -> Unit,
    onSetLipArea: (Long, UsageLipArea) -> Unit
) {
    if (bottles.isEmpty()) return

    item {
        Spacer(modifier = Modifier.height(4.dp))
        Text("Pigment Bottles Used", style = MaterialTheme.typography.titleMedium)
    }

    items(bottles, key = { it.id }) { bottle ->
        val isSelected = bottle.id in selectedBottleIds
        val bottleColor = remember(bottle.colorHex) {
            try {
                Color(android.graphics.Color.parseColor(bottle.colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleBottle(bottle.id) }
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(bottleColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(bottle.pigmentName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${bottle.pigmentBrand} - ${String.format("%.1f", bottle.remainingMl)} ml left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isSelected) {
                    var mlText by remember(bottle.id) { mutableStateOf("0.5") }
                    DasurvTextField(
                        value = mlText,
                        onValueChange = { newVal ->
                            mlText = newVal
                            newVal.toDoubleOrNull()?.let { onSetMlUsed(bottle.id, it) }
                        },
                        label = { Text("ml") },
                        modifier = Modifier.width(72.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            }
            if (isSelected) {
                Row(
                    modifier = Modifier.padding(start = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UsageLipArea.entries.forEach { area ->
                        FilterChip(
                            selected = false, // Parent manages selection
                            onClick = { onSetLipArea(bottle.id, area) },
                            label = {
                                Text(
                                    when (area) {
                                        UsageLipArea.UPPER -> "U"
                                        UsageLipArea.LOWER -> "L"
                                        UsageLipArea.BOTH -> "B"
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
