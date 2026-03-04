package com.dasurv.ui.screen.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatPrecise

@Composable
internal fun ConsumablesPage(
    consumables: List<Equipment>,
    selectedIds: Set<Long>,
    quantities: Map<Long, Double>,
    onToggle: (Long) -> Unit,
    onSetQuantity: (Long, Int) -> Unit,
) {
    if (consumables.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No consumables in inventory",
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
        verticalArrangement = Arrangement.spacedBy(DasurvTheme.spacing.sm),
    ) {
        Text(
            "Select Items",
            style = MaterialTheme.typography.labelLarge,
            color = M3OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp),
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                consumables.forEachIndexed { index, item ->
                    val isSelected = item.id in selectedIds
                    val qty = quantities[item.id] ?: 1.0
                    val qtyInt = qty.toInt().coerceAtLeast(1)
                    val remaining = item.stockQuantity - qtyInt

                    ConsumableRow(
                        item = item,
                        isSelected = isSelected,
                        qtyInt = qtyInt,
                        remaining = remaining,
                        onToggle = { onToggle(item.id) },
                        onSetQuantity = { onSetQuantity(item.id, it) },
                    )
                    if (index < consumables.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = M3Outline.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumableRow(
    item: Equipment,
    isSelected: Boolean,
    qtyInt: Int,
    remaining: Int,
    onToggle: () -> Unit,
    onSetQuantity: (Int) -> Unit,
) {
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
                colors = CheckboxDefaults.colors(checkedColor = M3CyanColor),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(M3CyanContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Healing, null, modifier = Modifier.size(18.dp), tint = M3CyanColor)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface,
                )
                Text(
                    "₱${item.costPerPiece.formatPrecise()} / piece" +
                        if (item.piecesPerPackage > 1) " · ${item.piecesPerPackage}/pkg" else "",
                    fontSize = 13.sp,
                    color = M3OnSurfaceVariant,
                )
            }
            if (!isSelected) {
                Text(
                    "${item.stockQuantity} in stock",
                    fontSize = 12.sp,
                    color = M3OnSurfaceVariant,
                )
            }
        }

        // Expanded stepper panel
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically() + fadeIn(),
        ) {
            val leftColor = when {
                remaining > 5 -> M3GreenColor
                remaining >= 1 -> M3AmberColor
                else -> M3RedColor
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 56.dp, end = 12.dp, bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = M3FieldBg,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Quantity",
                        fontSize = 13.sp,
                        color = M3OnSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    DasurvQuantityStepper(
                        value = qtyInt,
                        onValueChange = onSetQuantity,
                        minValue = 1,
                        maxValue = item.stockQuantity.coerceAtLeast(1),
                        accentColor = M3CyanColor,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "$remaining left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = leftColor,
                    )
                }
            }
        }
    }
}
