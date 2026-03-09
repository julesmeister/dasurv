package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.EquipmentPurchase
import com.dasurv.ui.component.*
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.FMT_DATE
import com.dasurv.util.formatCurrency
import com.dasurv.util.showDatePickerEndOfDay
import com.dasurv.util.showDatePickerStartOfDay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EquipmentPurchaseHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: EquipmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }

    val equipment by viewModel.equipment.collectAsStateWithLifecycle(initialValue = emptyList())
    val purchases by viewModel.allPurchases.collectAsStateWithLifecycle(initialValue = emptyList())
    val dateRange by viewModel.purchaseDateRange.collectAsStateWithLifecycle()

    val equipmentMap = remember(equipment) { equipment.associateBy { it.id } }

    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)
    var deleteTarget by remember { mutableStateOf<EquipmentPurchase?>(null) }
    var showRecordPurchase by remember { mutableStateOf(false) }
    val purchaseSources by viewModel.purchaseSources.collectAsStateWithLifecycle(initialValue = emptyList())
    val sellers by viewModel.sellers.collectAsStateWithLifecycle(initialValue = emptyList())

    if (showRecordPurchase) {
        EquipmentRecordPurchaseDialog(
            equipmentList = equipment,
            purchaseSources = purchaseSources,
            sellers = sellers,
            onDismiss = { showRecordPurchase = false },
            onConfirm = { equipmentId, qty, cost, date, notes, source, seller ->
                viewModel.recordPurchase(equipmentId, qty, cost, date, notes, source, seller) {
                    showRecordPurchase = false
                }
            }
        )
    }

    if (deleteTarget != null) {
        DasurvConfirmDialog(
            onDismissRequest = { deleteTarget = null },
            icon = Icons.Default.Delete,
            title = "Delete Purchase",
            message = "Remove this purchase record?",
            onConfirm = {
                viewModel.deletePurchase(deleteTarget!!) {}
                deleteTarget = null
            }
        )
    }

    Scaffold(
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Purchase History") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { showRecordPurchase = true },
                contentDescription = "Record Purchase"
            )
        }
    ) { padding ->
        var equipmentFilter by remember { mutableStateOf<Long?>(null) }
        var sourceFilter by remember { mutableStateOf<String?>(null) }

        val filteredPurchases = remember(purchases, equipmentFilter, sourceFilter) {
            purchases.filter { p ->
                (equipmentFilter == null || p.equipmentId == equipmentFilter) &&
                (sourceFilter == null || p.purchaseSource == sourceFilter)
            }
        }

        val equipmentNames = remember(purchases, equipmentMap) {
            purchases.map { it.equipmentId }.distinct()
                .mapNotNull { id -> equipmentMap[id]?.let { id to it.name } }
                .sortedBy { it.second }
        }
        val sources = remember(purchases) {
            purchases.map { it.purchaseSource }.filter { it.isNotBlank() }.distinct().sorted()
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(M3SurfaceContainer)
        ) {
            // Date range section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DasurvClickableField(
                    label = "From",
                    value = dateRange.first?.let { dateFormat.format(Date(it)) } ?: "",
                    placeholder = "Any",
                    onClick = {
                        showDatePickerStartOfDay(context, dateRange.first ?: System.currentTimeMillis()) {
                            viewModel.setPurchaseDateRange(it, dateRange.second)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                DasurvClickableField(
                    label = "To",
                    value = dateRange.second?.let { dateFormat.format(Date(it)) } ?: "",
                    placeholder = "Any",
                    onClick = {
                        showDatePickerEndOfDay(context, dateRange.second ?: System.currentTimeMillis()) {
                            viewModel.setPurchaseDateRange(dateRange.first, it)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // Equipment filter chips
            if (equipmentNames.size > 1) {
                DasurvChipSelectorRow(
                    items = equipmentNames,
                    selectedKey = equipmentFilter,
                    onSelect = { equipmentFilter = it },
                    allLabel = "All Items",
                    accentColor = M3CyanColor,
                    containerColor = M3CyanContainer,
                )
                HorizontalDivider(color = M3Outline, thickness = 1.dp)
            }

            // Source filter chips
            if (sources.size > 1) {
                val sourceItems = remember(sources) { sources.map { it to it } }
                DasurvChipSelectorRow(
                    items = sourceItems,
                    selectedKey = sourceFilter,
                    onSelect = { sourceFilter = it },
                    allLabel = "All Sources",
                    accentColor = M3IndigoColor,
                    containerColor = M3IndigoContainer,
                )
                HorizontalDivider(color = M3Outline, thickness = 1.dp)
            }

            // Summary strip
            val isFiltered = equipmentFilter != null || sourceFilter != null
            val totalCost = remember(filteredPurchases) { filteredPurchases.sumOf { it.totalCost } }
            val allCost = remember(purchases) { purchases.sumOf { it.totalCost } }
            DasurvSummaryStrip(
                label = if (isFiltered) "Filtered Total" else "All Purchases",
                value = "\u20B1${totalCost.formatCurrency()}",
                accentColor = M3CyanColor,
                secondaryLabel = if (isFiltered) "All Items" else null,
                secondaryValue = if (isFiltered) "\u20B1${allCost.formatCurrency()}" else null,
            )
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // Purchase list or empty state
            if (filteredPurchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DasurvEmptyState(
                        icon = Icons.Default.ShoppingCart,
                        message = if (dateRange.first != null || dateRange.second != null || equipmentFilter != null || sourceFilter != null)
                            "No matching purchases" else "No purchases recorded yet"
                    )
                }
            } else {
                @OptIn(ExperimentalFoundationApi::class)
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(filteredPurchases.size, key = { filteredPurchases[it].id }) { index ->
                        val purchase = filteredPurchases[index]
                        val eqName = equipmentMap[purchase.equipmentId]?.name ?: "Unknown"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = { deleteTarget = purchase }
                                )
                        ) {
                            M3ListRow(
                                icon = Icons.Default.ShoppingCart,
                                iconTint = M3CyanColor,
                                iconBg = M3CyanContainer,
                                label = eqName,
                                description = buildString {
                                    append("${purchase.quantity} pcs \u00b7 ${dateFormat.format(Date(purchase.purchaseDate))}")
                                    val sourceInfo = listOfNotNull(
                                        purchase.purchaseSource.ifBlank { null },
                                        purchase.seller.ifBlank { null }
                                    ).joinToString(" \u00b7 ")
                                    if (sourceInfo.isNotBlank()) append(" \u00b7 $sourceInfo")
                                },
                                trailing = {
                                    if (purchase.totalCost > 0) {
                                        M3ValueBadge(
                                            text = "\u20B1${purchase.totalCost.formatCurrency()}",
                                            color = M3CyanColor,
                                            containerColor = M3CyanContainer
                                        )
                                    }
                                }
                            )
                            if (purchase.notes.isNotBlank()) {
                                Text(
                                    purchase.notes,
                                    modifier = Modifier.padding(start = 74.dp, end = 16.dp, bottom = 12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = M3OnSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider(
                            color = M3Outline.copy(alpha = 0.5f), thickness = 1.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
        }
    }
}

