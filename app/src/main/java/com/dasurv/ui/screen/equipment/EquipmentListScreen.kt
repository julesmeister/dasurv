package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPurchaseHistory: () -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    // null = hidden, 0L = add new, >0 = edit existing
    var equipmentDialogId by remember { mutableStateOf<Long?>(null) }

    if (equipmentDialogId != null) {
        EquipmentFormDialog(
            equipmentId = equipmentDialogId?.takeIf { it != 0L },
            onDismiss = { equipmentDialogId = null }
        )
    }
    val spacing = DasurvTheme.spacing
    val equipment by viewModel.equipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)

    // Filter out pigment-category items (managed in Pigment Inventory)
    val nonPigmentEquipment = remember(equipment) {
        equipment.filter { it.category != "pigment" }
    }

    var categoryFilter by remember { mutableStateOf<String?>(null) }

    val filteredEquipment = remember(nonPigmentEquipment, typeFilter, categoryFilter) {
        nonPigmentEquipment.filter { item ->
            (typeFilter == null || item.type == typeFilter) &&
            (categoryFilter == null || when (categoryFilter) {
                "low_stock" -> isLowStock(item)
                else -> item.category == categoryFilter
            })
        }
    }

    var showUsageDialog by remember { mutableStateOf<Equipment?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Equipment?>(null) }
    var sheetItem by remember { mutableStateOf<Equipment?>(null) }

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

    if (showDeleteDialog != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = Icons.Default.Delete,
            title = "Delete",
            message = "Delete ${showDeleteDialog!!.name}?",
            onConfirm = {
                val item = showDeleteDialog!!
                viewModel.deleteEquipment(item) { showDeleteDialog = null }
            }
        )
    }

    if (sheetItem != null) {
        val item = sheetItem!!
        val isConsumable = item.type == "consumable"
        DasurvOptionsSheet(
            onDismiss = { sheetItem = null },
            icon = if (isConsumable) Icons.Default.Healing else Icons.Default.Build,
            iconBg = if (isConsumable) M3CyanContainer else M3PrimaryContainer,
            iconTint = if (isConsumable) M3CyanColor else M3Primary,
            title = item.name,
            subtitle = if (item.brand.isNotBlank()) item.brand else item.category.replaceFirstChar { it.uppercase() },
        ) {
            DasurvSheetOptionRow(
                icon = Icons.Default.Edit,
                iconBg = M3IndigoContainer,
                iconTint = M3IndigoColor,
                label = "Edit",
                subtitle = "Change equipment details",
                onClick = { sheetItem = null; equipmentDialogId = item.id },
            )
            if (isConsumable && item.stockQuantity > 0) {
                DasurvSheetOptionRow(
                    icon = Icons.Default.Healing,
                    iconBg = M3CyanContainer,
                    iconTint = M3CyanColor,
                    label = "Log Usage",
                    subtitle = "Record usage of this consumable",
                    onClick = { sheetItem = null; showUsageDialog = item },
                )
            }
            DasurvSheetOptionRow(
                icon = Icons.Default.Delete,
                iconBg = M3RedContainer,
                iconTint = M3RedColor,
                label = "Delete",
                subtitle = "Remove from inventory",
                onClick = { sheetItem = null; showDeleteDialog = item },
                isDestructive = true,
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Equipment & Products") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = onNavigateToPurchaseHistory) {
                        Icon(Icons.Default.History, "Purchase History")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { equipmentDialogId = 0L },
                contentDescription = "Add Equipment"
            )
        },
        containerColor = M3SurfaceContainer
    ) { padding ->
        val categories = remember(nonPigmentEquipment) {
            nonPigmentEquipment.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
        }
        val hasLowStock = remember(nonPigmentEquipment) {
            nonPigmentEquipment.any { isLowStock(it) }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(M3SurfaceContainer)
        ) {
            // Type filter chips
            val typeItems = remember { listOf("consumable" to "Consumables", "studio" to "Studio") }
            DasurvChipSelectorRow(
                items = typeItems,
                selectedKey = typeFilter,
                onSelect = { viewModel.setTypeFilter(it) },
                allLabel = "All",
                accentColor = M3Primary,
                containerColor = M3PrimaryContainer,
            )
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // Category filter chips
            if (categories.isNotEmpty() || hasLowStock) {
                val categoryItems = remember(categories, hasLowStock) {
                    categories.map { it to it.replaceFirstChar { c -> c.uppercase() } } +
                        if (hasLowStock) listOf("low_stock" to "Low Stock") else emptyList()
                }
                DasurvChipSelectorRow(
                    items = categoryItems,
                    selectedKey = categoryFilter,
                    onSelect = { categoryFilter = it },
                    allLabel = "All Categories",
                    accentColor = M3CyanColor,
                    containerColor = M3CyanContainer,
                )
                HorizontalDivider(color = M3Outline, thickness = 1.dp)
            }

            // Summary strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredEquipment.size} items",
                    fontSize = 13.sp, color = M3OnSurfaceVariant
                )
                if (typeFilter != null || categoryFilter != null) {
                    Text(
                        "${nonPigmentEquipment.size} total",
                        fontSize = 13.sp, color = M3OnSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // List or empty state
            if (filteredEquipment.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DasurvEmptyState(
                        icon = Icons.Default.Build,
                        message = if (typeFilter != null || categoryFilter != null)
                            "No matching equipment" else "No equipment added yet"
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(filteredEquipment.size, key = { filteredEquipment[it].id }) { index ->
                        val item = filteredEquipment[index]
                        EquipmentListItem(
                            item = item,
                            onClick = { equipmentDialogId = item.id },
                            onLongClick = { sheetItem = item },
                        )
                    }
                }
            }
        }
    }
}

