package com.dasurv.ui.screen.pigment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dasurv.data.model.Pigment
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.ColorSwatch
import com.dasurv.ui.theme.DasurvTheme

@Composable
internal fun PigmentDetailDialog(
    pigment: Pigment,
    onDismiss: () -> Unit,
    onAddToInventory: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pigment.name, color = M3OnSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorSwatch(colorHex = pigment.colorHex, label = "")
                    Spacer(modifier = Modifier.width(spacing.md))
                    Column {
                        Text(
                            pigment.brand.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = M3OnSurface
                        )
                        Text(
                            pigment.colorHex,
                            style = MaterialTheme.typography.bodySmall,
                            color = M3OnSurfaceVariant
                        )
                    }
                }
                if (pigment.undertone.isNotBlank() || pigment.intensity.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        if (pigment.undertone.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(pigment.undertone.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                        if (pigment.intensity.isNotBlank()) {
                            AssistChip(
                                onClick = {},
                                label = { Text(pigment.intensity.replaceFirstChar { it.uppercase() }) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
                if (pigment.description.isNotBlank()) {
                    Text(
                        pigment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = M3OnSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddToInventory,
                colors = ButtonDefaults.buttonColors(containerColor = M3Primary, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Opacity, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add to Inventory", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = M3OnSurfaceVariant) }
        }
    )
}
