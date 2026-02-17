package com.dasurv.ui.screen.camera

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.ui.theme.RosePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientPickerDialog(
    viewModel: LipCameraViewModel,
    onDismiss: () -> Unit,
    onClientSelected: (com.dasurv.data.local.entity.Client) -> Unit
) {
    val searchQuery by viewModel.clientSearchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.clientSearchResults.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadAllClients() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Client",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateClientSearch(it) },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RosePrimary,
                        cursorColor = RosePrimary
                    )
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(searchResults) { client ->
                        ListItem(
                            headlineContent = { Text(client.name) },
                            supportingContent = if (client.phone.isNotBlank()) {
                                { Text(client.phone) }
                            } else null,
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = RosePrimary
                                )
                            },
                            modifier = Modifier
                                .clickable { onClientSelected(client) }
                                .clip(RoundedCornerShape(8.dp))
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                    if (searchResults.isEmpty()) {
                        item {
                            Text(
                                "No clients found",
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
