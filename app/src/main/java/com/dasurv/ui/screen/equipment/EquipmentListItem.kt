package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EquipmentListItem(
    item: Equipment,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val spacing = DasurvTheme.spacing
    val isConsumable = item.type == "consumable"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        M3ListRow(
            icon = if (isConsumable) Icons.Default.Healing else Icons.Default.Build,
            iconTint = if (isConsumable) M3CyanColor else M3Primary,
            iconBg = if (isConsumable) M3CyanContainer else M3PrimaryContainer,
            label = item.name,
            description = item.brand.ifBlank { "No brand specified" },
            trailing = {
                if (isConsumable) {
                    M3StatusBadge(
                        text = stockBadgeText(item),
                        color = stockBadgeColor(item),
                        containerColor = stockBadgeContainer(item)
                    )
                }
            }
        )

        // Badges row below the main row
        Row(
            modifier = Modifier.padding(start = 70.dp, end = spacing.lg, bottom = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.category.isNotBlank()) {
                M3StatusBadge(
                    text = item.category.replaceFirstChar { it.uppercase() },
                    color = M3OnSurfaceVariant,
                    containerColor = M3FieldBg
                )
            }
            if (item.costPerUnit > 0) {
                M3ValueBadge(
                    text = "\u20B1${item.costPerUnit.formatCurrency()}",
                    color = M3Primary,
                    containerColor = M3PrimaryContainer.copy(alpha = 0.5f)
                )
            }
            if (isLowStock(item)) {
                M3StatusBadge(
                    text = "Low Stock",
                    color = M3AmberColor,
                    containerColor = M3AmberColor.copy(alpha = 0.1f)
                )
            }
        }
    }
    HorizontalDivider(
        color = M3Outline.copy(alpha = 0.5f), thickness = 1.dp,
        modifier = Modifier.padding(start = 72.dp)
    )
}
