package com.dasurv.ui.screen.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.data.model.CostSummary
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
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
        DasurvEmptyState(
            icon = Icons.Default.Palette,
            message = "No pigment bottles in stock",
        )
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
            M3SectionHeader(
                title = "Select Pigments",
                color = M3Primary,
                icon = Icons.Default.Palette,
                horizontalPadding = DasurvTheme.spacing.xl,
            )

            M3ListCard {
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
                            modifier = Modifier.padding(horizontal = DasurvTheme.spacing.lg),
                            color = M3Outline.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(DasurvTheme.spacing.lg))
        }

        // Cost summary section
        if (costSummary.items.isNotEmpty()) {
            M3SectionHeader(
                title = "Cost Summary",
                color = M3GreenColor,
                icon = Icons.Default.Payments,
                horizontalPadding = DasurvTheme.spacing.xl,
            )

            // Total card
            M3ListCard {
                // Prominent total row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DasurvTheme.spacing.lg, vertical = DasurvTheme.spacing.md),
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
                        Spacer(modifier = Modifier.width(DasurvTheme.spacing.md))
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
                        modifier = Modifier.padding(horizontal = DasurvTheme.spacing.lg),
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
                                .padding(horizontal = DasurvTheme.spacing.lg, vertical = DasurvTheme.spacing.md),
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
                                modifier = Modifier.padding(horizontal = DasurvTheme.spacing.lg),
                                color = M3Outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                            )
                        }
                    }
            }
        }
        Spacer(modifier = Modifier.height(DasurvTheme.spacing.lg))
    }
}