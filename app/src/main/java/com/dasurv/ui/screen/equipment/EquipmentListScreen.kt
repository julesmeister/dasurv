package com.dasurv.ui.screen.equipment

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Equipment
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    onNavigateBack: () -> Unit,
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter out pigment-category items (managed in Pigment Inventory)
    val nonPigmentEquipment = remember(equipment) {
        equipment.filter { it.category != "pigment" }
    }

    val filteredEquipment = remember(nonPigmentEquipment, typeFilter) {
        if (typeFilter == null) nonPigmentEquipment else nonPigmentEquipment.filter { it.type == typeFilter }
    }

    var showUsageDialog by remember { mutableStateOf<Equipment?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Equipment?>(null) }
    var sheetItem by remember { mutableStateOf<Equipment?>(null) }
    var showPurchaseHistory by remember { mutableStateOf(false) }
    var showRecordPurchase by remember { mutableStateOf(false) }

    val allPurchases by viewModel.allPurchases.collectAsStateWithLifecycle(initialValue = emptyList())
    val purchaseDateRange by viewModel.purchaseDateRange.collectAsStateWithLifecycle()
    val purchaseSources by viewModel.purchaseSources.collectAsStateWithLifecycle(initialValue = emptyList())
    val sellers by viewModel.sellers.collectAsStateWithLifecycle(initialValue = emptyList())

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

    if (showPurchaseHistory) {
        EquipmentPurchaseHistoryDialog(
            purchases = allPurchases,
            equipmentList = equipment,
            dateRange = purchaseDateRange,
            onDateRangeChange = { start, end -> viewModel.setPurchaseDateRange(start, end) },
            onDeletePurchase = { purchase -> viewModel.deletePurchase(purchase) {} },
            onRecordPurchase = { showRecordPurchase = true },
            onDismiss = { showPurchaseHistory = false }
        )
    }

    if (showRecordPurchase) {
        EquipmentRecordPurchaseDialog(
            equipmentList = equipment,
            purchaseSources = purchaseSources,
            sellers = sellers,
            onDismiss = { showRecordPurchase = false },
            onConfirm = { equipmentId, qty, cost, date, notes, source, sellerName ->
                viewModel.recordPurchase(equipmentId, qty, cost, date, notes, source, sellerName) {
                    showRecordPurchase = false
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
                actions = {
                    IconButton(onClick = { showPurchaseHistory = true }) {
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
                    DasurvFilterChip(
                        label = label,
                        selected = isSelected,
                        onClick = { viewModel.setTypeFilter(type) }
                    )
                }
            }

            Text(
                "${filteredEquipment.size} items",
                modifier = Modifier.padding(horizontal = spacing.lg),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )

            @OptIn(ExperimentalFoundationApi::class)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            M3ListCard {
                                filteredEquipment.forEachIndexed { index, item ->
                                    val isConsumable = item.type == "consumable"

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
                                                onClick = { equipmentDialogId = item.id },
                                                onLongClick = { sheetItem = item }
                                            )
                                    ) {
                                        M3ListRow(
                                            icon = if (isConsumable) Icons.Default.Healing else Icons.Default.Build,
                                            iconTint = if (isConsumable) M3CyanColor else M3Primary,
                                            iconBg = if (isConsumable) M3CyanContainer else M3PrimaryContainer,
                                            label = item.name,
                                            description = if (item.brand.isNotBlank()) item.brand else "",
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
                                            modifier = Modifier.padding(start = 70.dp, end = 16.dp, bottom = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                                                    text = "₱${item.costPerUnit.formatCurrency()}",
                                                    color = M3Primary,
                                                    containerColor = M3PrimaryContainer.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    if (index < filteredEquipment.lastIndex) {
                                        M3ListDivider()
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }
}
