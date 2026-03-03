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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.dasurv.util.formatMl

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
            .padding(DasurvTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.lg),
    ) {
        if (bottles.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.sm)) {
                Text(
                    "Pigment Bottles",
                    style = MaterialTheme.typography.labelLarge,
                    color = M3OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )

                DasurvFormCard {
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
                            M3ListDivider()
                        }
                    }
                }
            }
        }

        if (costSummary.items.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.sm)) {
                Text(
                    "Cost Summary",
                    style = MaterialTheme.typography.labelLarge,
                    color = M3OnSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )

                CostBreakdown(costSummary = costSummary)
            }
        }
    }
}

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

    AnimatedVisibility(
        visible = isSelected,
        enter = expandVertically() + fadeIn(),
    ) {
        Column(modifier = Modifier.padding(start = 92.dp, end = 16.dp, bottom = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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
                val leftColor = when {
                    afterPct > 0.5 -> M3GreenColor
                    afterPct >= 0.2 -> M3AmberColor
                    else -> M3RedColor
                }
                Text(
                    "${afterUse.formatMl()} left",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = leftColor,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
