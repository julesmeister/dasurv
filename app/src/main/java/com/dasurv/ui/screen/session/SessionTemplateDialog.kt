package com.dasurv.ui.screen.session

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.SessionTemplate
import com.dasurv.ui.component.*

@Composable
fun SessionTemplatePickerDialog(
    templates: List<SessionTemplate>,
    onSelectTemplate: (SessionTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = M3CyanColor)
        },
        title = {
            Text("Load Template", fontWeight = FontWeight.SemiBold)
        },
        text = {
            if (templates.isEmpty()) {
                Text(
                    "No templates available. Create one in Session Templates.",
                    color = M3OnSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(templates) { template ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onSelectTemplate(template) },
                            shape = MaterialTheme.shapes.medium,
                            color = M3FieldBg
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    template.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = M3OnSurface
                                )
                                if (template.procedure.isNotBlank()) {
                                    Text(
                                        template.procedure,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = M3OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = M3OnSurfaceVariant)
            }
        }
    )
}
