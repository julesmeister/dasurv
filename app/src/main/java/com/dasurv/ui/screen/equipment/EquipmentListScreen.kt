package com.dasurv.ui.screen.equipment

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEquipment: () -> Unit,
    onNavigateToEditEquipment: (Long) -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val equipment by viewModel.equipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Filter out pigment-category items (managed in Pigment Inventory)
    val nonPigmentEquipment = remember(equipment) {
        equipment.filter { it.category != "pigment" }
    }

    val filteredEquipment = remember(nonPigmentEquipment, typeFilter) {
        if (typeFilter == null) nonPigmentEquipment else nonPigmentEquipment.filter { it.type == typeFilter }
    }

    var showUsageDialog by remember { mutableStateOf<Equipment?>(null) }

    if (showUsageDialog != null) {
        EquipmentLogUsageDialog(
            equipment = showUsageDialog!!,
            onDismiss = { showUsageDialog = null },
            onConfirm = { qty, notes ->
                viewModel.logUsage(showUsageDialog!!.id, qty, notes) {
                    showUsageDialog = null
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Equipment & Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEquipment,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Default.Add, "Add Equipment")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterOptions = listOf(null to "All", "consumable" to "Consumables", "studio" to "Studio")
                filterOptions.forEach { (type, label) ->
                    val isSelected = typeFilter == type
                    FilledTonalButton(
                        onClick = { viewModel.setTypeFilter(type) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                "${filteredEquipment.size} items",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedContent(
                targetState = filteredEquipment.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "equipment-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No equipment added yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEquipment, key = { it.id }) { item ->
                            var showDeleteDialog by remember { mutableStateOf(false) }

                            if (showDeleteDialog) {
                                DasurvConfirmDialog(
                                    onDismissRequest = { showDeleteDialog = false },
                                    icon = Icons.Default.Delete,
                                    title = "Delete",
                                    message = "Delete ${item.name}?",
                                    onConfirm = { viewModel.deleteEquipment(item) { showDeleteDialog = false } }
                                )
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                onClick = { onNavigateToEditEquipment(item.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                                            AssistChip(
                                                onClick = {},
                                                label = {
                                                    Text(
                                                        item.type.replaceFirstChar { it.uppercase() },
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                },
                                                modifier = Modifier.height(24.dp)
                                            )
                                        }
                                        if (item.category.isNotBlank()) {
                                            Text(
                                                item.category,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (item.type == "consumable") {
                                            Text(
                                                "\$${String.format("%.2f", item.costPerUnit)} / pkg" +
                                                    if (item.piecesPerPackage > 1)
                                                        " (${item.piecesPerPackage} pcs, \$${String.format("%.4f", item.costPerPiece)}/pc)"
                                                    else "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            if (item.costPerUnit > 0) {
                                                Text(
                                                    "\$${String.format("%.2f", item.costPerUnit)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (item.type == "consumable") {
                                            TextButton(onClick = { showUsageDialog = item }) {
                                                Text("Log Use")
                                            }
                                        }
                                    }
                                    IconButton(onClick = { showDeleteDialog = true }) {
                                        Icon(Icons.Default.Delete, "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
