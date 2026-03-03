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
            .padding(horizontal = DasurvTheme.spacing.md),
    ) {
        consumables.forEachIndexed { index, item ->
            val isSelected = item.id in selectedIds
            val qty = quantities[item.id] ?: 1.0
            val qtyInt = qty.toInt().coerceAtLeast(1)
            val remaining = item.stockQuantity - qtyInt

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle(item.id) },
                    colors = CheckboxDefaults.colors(checkedColor = M3Primary),
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
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
            ) {
                val leftColor = when {
                    remaining > 5 -> M3GreenColor
                    remaining >= 1 -> M3AmberColor
                    else -> M3RedColor
                }
                Row(
                    modifier = Modifier.padding(start = 92.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DasurvQuantityStepper(
                        value = qtyInt,
                        onValueChange = { onSetQuantity(item.id, it) },
                        minValue = 1,
                        maxValue = item.stockQuantity.coerceAtLeast(1),
                        accentColor = M3CyanColor,
                    )
                    Text(
                        "$remaining left",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = leftColor,
                    )
                }
            }
            if (index < consumables.lastIndex) {
                M3ListDivider()
            }
        }
    }
}
