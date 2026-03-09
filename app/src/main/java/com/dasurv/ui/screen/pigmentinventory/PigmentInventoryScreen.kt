package com.dasurv.ui.screen.pigmentinventory

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
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
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
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

    // Pigment stock form dialog
    var showStockDialog by remember { mutableStateOf(false) }
    var editingStockId by remember { mutableStateOf<Long?>(null) }

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

    if (showStockDialog || editingStockId != null) {
        PigmentStockFormDialog(
            equipmentId = editingStockId,
            onDismiss = {
                showStockDialog = false
                editingStockId = null
            },
            viewModel = viewModel,
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
                onClick = { showStockDialog = true },
                contentDescription = "Add Pigment"
            )
        },
        snackbarHost = { M3SnackbarHost(snackbarHostState) }
    ) { padding ->
        val isEmpty = filteredStock.isEmpty() && filteredStandalone.isEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Brand filter chips — always visible
            if (brands.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    item {
                        DasurvFilterChip(
                            label = "All",
                            selected = brandFilter == null,
                            onClick = { viewModel.setBrandFilter(null) }
                        )
                    }
                    items(brands) { brand ->
                        DasurvFilterChip(
                            label = brand,
                            selected = brandFilter == brand,
                            onClick = { viewModel.setBrandFilter(brand) }
                        )
                    }
                }
            }

            // Stats summary tabs — always visible
            LazyRow(
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.md),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                item {
                    M3SummaryTab(
                        value = "${filteredStock.size}",
                        label = "Pigments",
                        icon = Icons.Default.Palette,
                        color = M3PurpleColor,
                        isActive = false,
                        onClick = {}
                    )
                }
                item {
                    M3SummaryTab(
                        value = "$totalStock",
                        label = "In Stock",
                        icon = Icons.Default.Inventory,
                        color = M3GreenColor,
                        isActive = false,
                        onClick = {}
                    )
                }
                item {
                    M3SummaryTab(
                        value = "$totalOpenBottles",
                        label = "Open Bottles",
                        icon = Icons.Default.Opacity,
                        color = M3SkyBlueColor,
                        isActive = false,
                        onClick = {}
                    )
                }
            }

            // List content or empty state
            AnimatedContent(
                targetState = isEmpty,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "pigment-inventory-state",
                modifier = Modifier.fillMaxSize()
            ) { showEmpty ->
                if (showEmpty) {
                    DasurvEmptyState(
                        icon = Icons.Default.Opacity,
                        message = "No pigments tracked yet"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
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
                                onEdit = { editingStockId = stockItem.id },
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
                                M3SectionHeader("Other Bottles", M3OnSurfaceVariant)
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
        }
    }
}
