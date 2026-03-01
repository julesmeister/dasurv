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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

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
    val color = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }
    val spacing = DasurvTheme.spacing

    M3ListCard {
        Column(modifier = Modifier.padding(spacing.lg)) {
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
                Spacer(modifier = Modifier.width(spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        equipment.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurface
                    )
                    Text(
                        equipment.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
                // Stock badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (equipment.stockQuantity > 0) M3PrimaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "${equipment.stockQuantity} in stock",
                        modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (equipment.stockQuantity > 0) M3Primary
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Cost info
            if (equipment.costPerUnit > 0) {
                Text(
                    "\$${String.format("%.2f", equipment.costPerUnit)}/bottle",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3Primary,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }

            // Open bottles count
            if (openBottleCount > 0) {
                Text(
                    "$openBottleCount open bottle${if (openBottleCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(spacing.sm))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                FilledTonalButton(
                    onClick = onOpenBottle,
                    enabled = equipment.stockQuantity > 0,
                    contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.xs),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = M3PrimaryContainer,
                        contentColor = M3Primary
                    )
                ) {
                    Icon(Icons.Default.Opacity, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text("Open", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = onRestock,
                    contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.xs),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = M3PrimaryContainer,
                        contentColor = M3Primary
                    )
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text("Restock", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(16.dp), tint = M3OnSurfaceVariant)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(16.dp), tint = M3OnSurfaceVariant)
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
                        modifier = Modifier.size(18.dp),
                        tint = M3Primary
                    )
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Text(
                        if (isExpanded) "Hide bottles" else "Show ${openBottles.size} bottle${if (openBottles.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = M3Primary
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        openBottles.forEachIndexed { index, bottle ->
                            BottleRow(
                                bottle = bottle,
                                onClick = { onBottleClick(bottle) },
                                onLogUse = { onLogUse(bottle) }
                            )
                            if (index < openBottles.lastIndex) {
                                M3ListDivider()
                            }
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
    val spacing = DasurvTheme.spacing
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = M3SurfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { bottle.usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (bottle.usagePercentage < 0.2f)
                        MaterialTheme.colorScheme.error
                    else M3Primary,
                    trackColor = M3Outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${String.format("%.1f", bottle.remainingMl)}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = M3OnSurfaceVariant
                    )
                    if (bottle.pricePerMl > 0) {
                        Text(
                            "\$${String.format("%.2f", bottle.remainingValue)} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = M3Primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            TextButton(
                onClick = onLogUse,
                contentPadding = PaddingValues(horizontal = spacing.sm, vertical = 2.dp)
            ) {
                Text("Log Use", style = MaterialTheme.typography.labelSmall, color = M3Primary)
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
    val color = remember(bottle.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(bottle.colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }
    val spacing = DasurvTheme.spacing

    M3ListCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
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
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurface
                )
                Text(
                    bottle.pigmentBrand,
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                LinearProgressIndicator(
                    progress = { bottle.usagePercentage.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (bottle.usagePercentage < 0.2f)
                        MaterialTheme.colorScheme.error
                    else M3Primary,
                    trackColor = M3Outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${String.format("%.1f", bottle.remainingMl)}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            FilledTonalButton(
                onClick = onLogUse,
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.xs),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = M3PrimaryContainer,
                    contentColor = M3Primary
                )
            ) {
                Text("Log Use", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
