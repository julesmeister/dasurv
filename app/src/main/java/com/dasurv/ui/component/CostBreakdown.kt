package com.dasurv.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dasurv.data.model.CostItem
import com.dasurv.data.model.CostSummary
import com.dasurv.util.formatCurrency
import com.dasurv.util.formatPrecise

@Composable
fun CostBreakdown(
    costSummary: CostSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Total row — prominent at top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = M3OnSurface,
                )
                Text(
                    "₱${costSummary.totalCost.formatCurrency()}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = M3Primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = M3Outline.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Line items
            costSummary.items.forEachIndexed { index, item ->
                CostItemRow(item)
                if (index < costSummary.items.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = M3Outline.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CostItemRow(item: CostItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = M3OnSurface,
            )
            val qtyLabel = item.quantity.let {
                if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
            }
            Text(
                "Qty: $qtyLabel  @  ₱${item.unitCost.formatPrecise()}",
                fontSize = 12.sp,
                color = M3OnSurfaceVariant,
            )
            if (item.perPieceInfo.isNotBlank()) {
                Text(
                    item.perPieceInfo,
                    fontSize = 11.sp,
                    color = M3OnSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
        Text(
            "₱${item.totalCost.formatCurrency()}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = M3OnSurface,
        )
    }
}
