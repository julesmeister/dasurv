package com.dasurv.ui.screen.equipment

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvConfirmDialog
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3ListCard
import com.dasurv.ui.component.M3ListDivider
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEquipment: () -> Unit,
    onNavigateToEditEquipment: (Long) -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val spacing = DasurvTheme.spacing
    val equipment by viewModel.equipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

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
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Equipment & Products") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = onNavigateToAddEquipment,
                contentDescription = "Add Equipment"
            )
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Type filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                val filterOptions = listOf(null to "All", "consumable" to "Consumables", "studio" to "Studio")
                filterOptions.forEach { (type, label) ->
                    val isSelected = typeFilter == type
                    FilledTonalButton(
                        onClick = { viewModel.setTypeFilter(type) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isSelected)
                                M3PrimaryContainer
                            else
                                Color(0xFFF0F1FA),
                            contentColor = if (isSelected)
                                M3Primary
                            else
                                M3OnSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm)
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
                modifier = Modifier.padding(horizontal = spacing.lg),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )

            AnimatedContent(
                targetState = filteredEquipment.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "equipment-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    DasurvEmptyState(
                        icon = Icons.Default.Build,
                        message = "No equipment added yet"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
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

                            M3ListCard(modifier = Modifier.animateItem()) {
                                Surface(
                                    onClick = { onNavigateToEditEquipment(item.id) },
                                    color = androidx.compose.ui.graphics.Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(spacing.lg),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                                            ) {
                                                Text(
                                                    item.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = M3OnSurface
                                                )
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
                                                    color = M3OnSurfaceVariant
                                                )
                                            }
                                            if (item.type == "consumable") {
                                                Text(
                                                    "\$${String.format("%.2f", item.costPerUnit)} / pkg" +
                                                        if (item.piecesPerPackage > 1)
                                                            " (${item.piecesPerPackage} pcs, \$${String.format("%.4f", item.costPerPiece)}/pc)"
                                                        else "",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = M3Primary
                                                )
                                            } else {
                                                if (item.costPerUnit > 0) {
                                                    Text(
                                                        "\$${String.format("%.2f", item.costPerUnit)}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = M3Primary
                                                    )
                                                }
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            if (item.type == "consumable") {
                                                TextButton(onClick = { showUsageDialog = item }) {
                                                    Text("Log Use", color = M3Primary)
                                                }
                                            }
                                        }
                                        IconButton(onClick = { showDeleteDialog = true }) {
                                            Icon(Icons.Default.Delete, "Delete", tint = M3OnSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            M3ListDivider()
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}
