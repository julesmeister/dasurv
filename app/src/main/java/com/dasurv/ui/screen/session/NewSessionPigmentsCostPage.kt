package com.dasurv.ui.screen.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.data.model.CostSummary
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.ui.util.parseHexSafe
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatMl
import com.dasurv.util.formatPrecise

@Composable
internal fun PigmentsCostPage(
    bottles: List<PigmentBottle>,
    selectedBottleIds: Set<Long>,
    bottleEntries: Map<Long, PigmentBottleSessionEntry>,
    onToggleBottle: (Long) -> Unit,
    onSetMlUsed: (Long, Double) -> Unit,
    onSetLipArea: (Long, UsageLipArea) -> Unit,
    costSummary: CostSummary,
) {
    if (bottles.isEmpty() && costSummary.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No pigment bottles in stock",
                color = M3OnSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = DasurvTheme.spacing.md),
    ) {
        // Pigment bottles section
        if (bottles.isNotEmpty()) {
            SectionHeader(
                icon = Icons.Default.Palette,
                title = "Select Pigments",
                accentColor = M3Primary,
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    bottles.forEachIndexed { index, bottle ->
                        BottleRow(
                            bottle = bottle,
                            isSelected = bottle.id in selectedBottleIds,
                            entry = bottleEntries[bottle.id],
                            onToggle = { onToggleBottle(bottle.id) },
                            onSetMlUsed = { onSetMlUsed(bottle.id, it) },
                            onSetLipArea = { onSetLipArea(bottle.id, it) },
                        )
                        if (index < bottles.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = M3Outline.copy(alpha = 0.5f),
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cost summary section
        if (costSummary.items.isNotEmpty()) {
            SectionHeader(
                icon = Icons.Default.Payments,
                title = "Cost Summary",
                accentColor = M3GreenColor,
            )

            // Total card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    // Prominent total row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(M3GreenColor.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Payments, null,
                                modifier = Modifier.size(20.dp),
                                tint = M3GreenColor,
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Total",
                                fontSize = 12.sp,
                                color = M3OnSurfaceVariant,
                            )
                            Text(
                                "₱${costSummary.totalCost.formatCurrency()}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = M3OnSurface,
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = M3Outline.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                    )

                    // Line items
                    costSummary.items.forEachIndexed { index, item ->
                        val qtyLabel = item.quantity.let {
                            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                item.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = M3OnSurface,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = M3OnSurfaceVariant.copy(alpha = 0.08f),
                                ) {
                                    Text(
                                        "Qty: $qtyLabel",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = M3OnSurfaceVariant,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = M3OnSurfaceVariant.copy(alpha = 0.08f),
                                ) {
                                    Text(
                                        "₱${item.unitCost.formatPrecise()} ea",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = M3OnSurfaceVariant,
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = M3GreenColor.copy(alpha = 0.08f),
                                ) {
                                    Text(
                                        "₱${item.totalCost.formatCurrency()}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3GreenColor,
                                    )
                                }
                            }
                        }
                        if (index < costSummary.items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = M3Outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── Section header with icon ────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    accentColor: Color,
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon, null,
            modifier = Modifier.size(18.dp),
            tint = accentColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor,
            letterSpacing = 0.5.sp,
        )
    }
}

// ── Bottle row with expandable detail panel ─────────────────────────────────

@Composable
private fun BottleRow(
    bottle: PigmentBottle,
    isSelected: Boolean,
    entry: PigmentBottleSessionEntry?,
    onToggle: () -> Unit,
    onSetMlUsed: (Double) -> Unit,
    onSetLipArea: (UsageLipArea) -> Unit,
) {
    val bottleColor = remember(bottle.colorHex) { parseHexSafe(bottle.colorHex) }
    val mlUsed = entry?.mlUsed ?: 0.5
    val afterUse = (bottle.remainingMl - mlUsed).coerceAtLeast(0.0)
    val afterPct = if (bottle.bottleSizeMl > 0) afterUse / bottle.bottleSizeMl else 0.0

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = M3Primary),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bottleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(bottleColor)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bottle.pigmentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface,
                )
                Text(
                    "${bottle.pigmentBrand} · ${bottle.remainingMl.formatMl()} ml",
                    fontSize = 13.sp,
                    color = M3OnSurfaceVariant,
                )
            }
        }

        // Expanded usage details
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 12.dp, bottom = 10.dp),
            ) {
                // Amount row — icon box + label + input + remaining pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(M3Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Opacity, null,
                            modifier = Modifier.size(18.dp),
                            tint = M3Primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurface,
                        modifier = Modifier.weight(1f),
                    )
                    var mlText by remember(bottle.id, entry?.mlUsed) {
                        mutableStateOf(entry?.mlUsed?.let {
                            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                        } ?: "0.5")
                    }
                    DasurvTextField(
                        value = mlText,
                        onValueChange = { newVal ->
                            mlText = newVal
                            newVal.toDoubleOrNull()?.let { onSetMlUsed(it) }
                        },
                        label = "ml",
                        modifier = Modifier.width(64.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        autoCapitalize = false,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val leftColor = when {
                        afterPct > 0.5 -> M3GreenColor
                        afterPct >= 0.2 -> M3AmberColor
                        else -> M3RedColor
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = leftColor.copy(alpha = 0.15f),
                    ) {
                        Text(
                            "${afterUse.formatMl()} left",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = leftColor,
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = M3Outline.copy(alpha = 0.3f),
                    thickness = 0.5.dp,
                )

                // Lip area row — icon box + label + chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(M3Primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Colorize, null,
                            modifier = Modifier.size(18.dp),
                            tint = M3Primary,
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Lip Area",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        UsageLipArea.entries.forEach { area ->
                            val isAreaSelected = (entry?.lipArea ?: UsageLipArea.BOTH) == area
                            DasurvFilterChip(
                                label = when (area) {
                                    UsageLipArea.UPPER -> "Upper"
                                    UsageLipArea.LOWER -> "Lower"
                                    UsageLipArea.BOTH -> "Both"
                                },
                                selected = isAreaSelected,
                                onClick = { onSetLipArea(area) },
                            )
                        }
                    }
                }
            }
        }
    }
}
