package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.dasurv.ui.util.parseHexSafe
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatMl

@OptIn(ExperimentalFoundationApi::class)
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
    val color = remember(colorHex) { parseHexSafe(colorHex) }
    val spacing = DasurvTheme.spacing
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        DasurvOptionsSheet(
            onDismiss = { showSheet = false },
            icon = Icons.Default.Palette,
            iconBg = color.copy(alpha = 0.15f),
            iconTint = color,
            title = equipment.name,
            subtitle = equipment.brand,
        ) {
            DasurvSheetOptionRow(
                icon = Icons.Default.Edit,
                iconBg = M3IndigoContainer,
                iconTint = M3IndigoColor,
                label = "Edit",
                subtitle = "Change pigment details",
                onClick = { showSheet = false; onEdit() },
            )
            if (equipment.stockQuantity > 0) {
                DasurvSheetOptionRow(
                    icon = Icons.Default.Opacity,
                    iconBg = M3CyanContainer,
                    iconTint = M3CyanColor,
                    label = "Open Bottle",
                    subtitle = "Open a new bottle from stock",
                    onClick = { showSheet = false; onOpenBottle() },
                )
            }
            DasurvSheetOptionRow(
                icon = Icons.Default.Add,
                iconBg = M3GreenContainer,
                iconTint = M3GreenColor,
                label = "Restock",
                subtitle = "Add bottles to inventory",
                onClick = { showSheet = false; onRestock() },
            )
            DasurvSheetOptionRow(
                icon = Icons.Default.Delete,
                iconBg = M3RedContainer,
                iconTint = M3RedColor,
                label = "Delete",
                subtitle = "Remove from inventory",
                onClick = { showSheet = false; onDelete() },
                isDestructive = true,
            )
        }
    }

    M3ListCard(
        modifier = Modifier.combinedClickable(
            onClick = onEdit,
            onLongClick = { showSheet = true }
        )
    ) {
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
                M3StatusBadge(
                    text = "${equipment.stockQuantity} in stock",
                    color = if (equipment.stockQuantity > 0) M3GreenColor else M3RedColor,
                    containerColor = if (equipment.stockQuantity > 0) M3GreenContainer else M3RedContainer
                )
            }

            // Badges row
            Row(
                modifier = Modifier.padding(start = 52.dp, top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (equipment.costPerUnit > 0) {
                    M3ValueBadge(
                        text = "₱${equipment.costPerUnit.formatCurrency()}/bottle",
                        color = M3Primary,
                        containerColor = M3PrimaryContainer.copy(alpha = 0.5f)
                    )
                }
                if (openBottleCount > 0) {
                    M3StatusBadge(
                        text = "$openBottleCount open",
                        color = M3CyanColor,
                        containerColor = M3CyanContainer
                    )
                }
            }

            // Expandable bottles section
            if (openBottles.isNotEmpty()) {
                TextButton(
                    onClick = onToggleExpand,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.padding(start = 40.dp)
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
                        M3RedColor
                    else M3Primary,
                    trackColor = M3Outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${bottle.remainingMl.formatMl()}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = M3OnSurfaceVariant
                    )
                    if (bottle.pricePerMl > 0) {
                        Text(
                            "\$${bottle.remainingValue.formatCurrency()} left",
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
    val color = remember(bottle.colorHex) { parseHexSafe(bottle.colorHex) }
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
                        M3RedColor
                    else M3Primary,
                    trackColor = M3Outline.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${bottle.remainingMl.formatMl()}/${String.format("%.0f", bottle.bottleSizeMl)} ml",
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
