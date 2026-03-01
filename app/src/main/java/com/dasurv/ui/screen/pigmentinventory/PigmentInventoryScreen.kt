package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.dasurv.ui.component.DasurvConfirmDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.Client
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.PigmentBottle
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PigmentInventoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddStock: () -> Unit,
    onNavigateToEditStock: (Long) -> Unit,
    onNavigateToEditBottle: (Long) -> Unit,
    onNavigateToCatalogue: () -> Unit = {},
    viewModel: PigmentInventoryViewModel = hiltViewModel()
) {
    val pigmentStock by viewModel.pigmentStock.collectAsStateWithLifecycle(initialValue = emptyList())
    val bottles by viewModel.allBottles.collectAsStateWithLifecycle(initialValue = emptyList())
    val clients by viewModel.allClients.collectAsStateWithLifecycle(initialValue = emptyList())
    val brandFilter by viewModel.brandFilter.collectAsStateWithLifecycle()
    val allPigments = viewModel.allPigments
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val spacing = DasurvTheme.spacing

    // Group bottles by equipmentId
    val bottlesByEquipmentId = remember(bottles) {
        bottles.filter { it.equipmentId != null }.groupBy { it.equipmentId!! }
    }
    val standaloneBottles = remember(bottles) {
        bottles.filter { it.equipmentId == null }
    }

    // Color lookup from catalog
    val colorMap = remember(pigmentStock, allPigments) {
        pigmentStock.associate { eq ->
            eq.id to (allPigments.find { it.name == eq.name }?.colorHex ?: "#CCCCCC")
        }
    }

    // Brand list from stock items
    val brands = remember(pigmentStock) {
        pigmentStock.map { it.brand }.distinct().sorted()
    }

    // Apply brand filter
    val filteredStock = remember(pigmentStock, brandFilter) {
        if (brandFilter == null) pigmentStock
        else pigmentStock.filter { it.brand == brandFilter }
    }
    val filteredStandalone = remember(standaloneBottles, brandFilter) {
        if (brandFilter == null) standaloneBottles
        else standaloneBottles.filter { it.pigmentBrand == brandFilter }
    }

    // Stats
    val totalStock = pigmentStock.sumOf { it.stockQuantity }
    val totalOpenBottles = bottles.count { it.remainingMl > 0 }

    // Dialog states
    var showLogDialog by remember { mutableStateOf(false) }
    var logBottle by remember { mutableStateOf<PigmentBottle?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var restockEquipment by remember { mutableStateOf<Equipment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteEquipment by remember { mutableStateOf<Equipment?>(null) }

    // Expanded stock items (to show/hide their bottles)
    var expandedStockIds by remember { mutableStateOf(setOf<Long>()) }

    if (showLogDialog && logBottle != null) {
        LogUsageDialog(
            bottle = logBottle!!,
            clients = clients,
            onDismiss = { showLogDialog = false },
            onConfirm = { clientId, lipArea, mlUsed, notes ->
                viewModel.logUsage(
                    bottleId = logBottle!!.id,
                    clientId = clientId,
                    lipArea = lipArea,
                    mlUsed = mlUsed,
                    notes = notes
                )
                showLogDialog = false
            }
        )
    }

    if (showRestockDialog && restockEquipment != null) {
        RestockDialog(
            equipment = restockEquipment!!,
            onDismiss = { showRestockDialog = false },
            onConfirm = { count ->
                viewModel.restock(restockEquipment!!, count)
                showRestockDialog = false
            }
        )
    }

    if (showDeleteDialog && deleteEquipment != null) {
        DasurvConfirmDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = Icons.Default.Delete,
            title = "Delete",
            message = "Delete ${deleteEquipment!!.name} and all its opened bottles?",
            onConfirm = { viewModel.deleteStock(deleteEquipment!!) { showDeleteDialog = false } }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle(title = "Pigment Inventory") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                actions = {
                    IconButton(onClick = onNavigateToCatalogue) {
                        Icon(Icons.Default.Palette, "Pigment Catalogue", tint = M3OnSurface)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = onNavigateToAddStock,
                contentDescription = "Add Pigment"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            // Brand filter chips
            if (brands.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        item {
                            FilterChip(
                                selected = brandFilter == null,
                                onClick = { viewModel.setBrandFilter(null) },
                                label = { Text("All") }
                            )
                        }
                        items(brands) { brand ->
                            FilterChip(
                                selected = brandFilter == brand,
                                onClick = { viewModel.setBrandFilter(brand) },
                                label = { Text(brand) }
                            )
                        }
                    }
                }
            }

            // Stats
            item {
                Text(
                    "${filteredStock.size} pigments | $totalStock in stock | $totalOpenBottles open",
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
            }

            // Empty state
            if (filteredStock.isEmpty() && filteredStandalone.isEmpty()) {
                item {
                    DasurvEmptyState(
                        icon = Icons.Default.Opacity,
                        message = "No pigments tracked yet"
                    )
                }
            }

            // Stock items with their bottles
            items(filteredStock, key = { it.id }) { stockItem ->
                val stockBottles = bottlesByEquipmentId[stockItem.id] ?: emptyList()
                val openBottles = stockBottles.filter { it.remainingMl > 0 }
                val isExpanded = expandedStockIds.contains(stockItem.id)
                val stockColor = colorMap[stockItem.id] ?: "#CCCCCC"

                StockItemCard(
                    equipment = stockItem,
                    colorHex = stockColor,
                    openBottleCount = openBottles.size,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedStockIds = if (isExpanded) {
                            expandedStockIds - stockItem.id
                        } else {
                            expandedStockIds + stockItem.id
                        }
                    },
                    onEdit = { onNavigateToEditStock(stockItem.id) },
                    onOpenBottle = {
                        viewModel.openBottle(stockItem) { bottleId ->
                            onNavigateToEditBottle(bottleId)
                        }
                    },
                    onRestock = {
                        restockEquipment = stockItem
                        showRestockDialog = true
                    },
                    onDelete = {
                        deleteEquipment = stockItem
                        showDeleteDialog = true
                    },
                    openBottles = openBottles,
                    onBottleClick = { onNavigateToEditBottle(it.id) },
                    onLogUse = { bottle ->
                        logBottle = bottle
                        showLogDialog = true
                    }
                )
            }

            // Standalone bottles (not linked to stock)
            if (filteredStandalone.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(spacing.sm))
                    Text(
                        "Other Bottles",
                        style = MaterialTheme.typography.titleSmall,
                        color = M3OnSurfaceVariant
                    )
                }
                items(filteredStandalone, key = { it.id }) { bottle ->
                    StandaloneBottleCard(
                        bottle = bottle,
                        onClick = { onNavigateToEditBottle(bottle.id) },
                        onLogUse = {
                            logBottle = bottle
                            showLogDialog = true
                        }
                    )
                }
            }
        }
    }
}
