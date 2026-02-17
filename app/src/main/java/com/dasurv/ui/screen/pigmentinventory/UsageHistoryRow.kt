package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.PigmentBottleUsage
import com.dasurv.data.local.entity.UsageLipArea
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun UsageHistoryRow(usage: PigmentBottleUsage) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dateFormat.format(Date(usage.date)),
                    style = MaterialTheme.typography.bodyMedium
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (usage.notes.isNotBlank()) {
                    Text(
                        usage.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            if (usage.costAtTimeOfUse > 0) {
                Text(
                    "$${String.format("%.2f", usage.costAtTimeOfUse)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
