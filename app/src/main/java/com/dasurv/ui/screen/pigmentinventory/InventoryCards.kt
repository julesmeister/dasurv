package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle

@Composable
internal fun StockItemCard(
    equipment: Equipment,
    colorHex: String,
    openBottleCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onOpenBottle: () -> Unit,
    onRestock: () -> Unit,
    onDelete: () -> Unit,
    openBottles: List<PigmentBottle>,
    onBottleClick: (PigmentBottle) -> Unit,
    onLogUse: (PigmentBottle) -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        equipment.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        equipment.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Stock badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (equipment.stockQuantity > 0)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "${equipment.stockQuantity} in stock",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (equipment.stockQuantity > 0)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Cost info
            if (equipment.costPerUnit > 0) {
                Text(
                    "\$${String.format("%.2f", equipment.costPerUnit)}/bottle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Open bottles count
            if (openBottleCount > 0) {
                Text(
                    "$openBottleCount open bottle${if (openBottleCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onOpenBottle,
                    enabled = equipment.stockQuantity > 0,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Opacity, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = onRestock,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restock", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp))
                }
            }

            // Expandable bottles section
            if (openBottles.isNotEmpty()) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isExpanded) "Hide bottles" else "Show ${openBottles.size} bottle${if (openBottles.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        openBottles.forEach { bottle ->
                            BottleRow(
                                bottle = bottle,
                                onClick = { onBottleClick(bottle) },
                                onLogUse = { onLogUse(bottle) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BottleRow(
    bottle: PigmentBottle,
    onClick: () -> Unit,
    onLogUse: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { bottle.usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (bottle.usagePercentage < 0.2f)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${String.format("%.1f", bottle.remainingMl)}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (bottle.pricePerMl > 0) {
                        Text(
                            "\$${String.format("%.2f", bottle.remainingValue)} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onLogUse,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Log Use", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
internal fun StandaloneBottleCard(
    bottle: PigmentBottle,
    onClick: () -> Unit,
    onLogUse: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(bottle.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bottle.pigmentName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    bottle.pigmentBrand,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { bottle.usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (bottle.usagePercentage < 0.2f)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${String.format("%.1f", bottle.remainingMl)}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onLogUse,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Log Use", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
