package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.UsageLipArea
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.theme.DasurvTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun UsageHistoryRow(usage: PigmentBottleUsage) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val spacing = DasurvTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                dateFormat.format(Date(usage.date)),
                style = MaterialTheme.typography.bodyMedium,
                color = M3OnSurface
            )
            Text(
                "${String.format("%.2f", usage.mlUsed)} ml - ${
                    when (usage.lipArea) {
                        UsageLipArea.UPPER -> "Upper Lip"
                        UsageLipArea.LOWER -> "Lower Lip"
                        UsageLipArea.BOTH -> "Both Lips"
                    }
                }",
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )
            if (usage.notes.isNotBlank()) {
                Text(
                    usage.notes,
                    style = MaterialTheme.typography.labelSmall,
                    color = M3OnSurfaceVariant
                )
            }
        }
        if (usage.costAtTimeOfUse > 0) {
            Text(
                "$${String.format("%.2f", usage.costAtTimeOfUse)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = M3Primary
            )
        }
    }
}
