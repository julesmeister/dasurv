package com.dasurv.ui.screen.equipment

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dasurv.data.local.entity.Equipment
import com.dasurv.data.local.entity.EquipmentPurchase
import com.dasurv.ui.component.*
import com.dasurv.util.FMT_DATE
import com.dasurv.util.formatCurrency
import com.dasurv.util.showDatePickerEndOfDay
import com.dasurv.util.showDatePickerStartOfDay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EquipmentPurchaseHistoryDialog(
    purchases: List<EquipmentPurchase>,
    equipmentList: List<Equipment>,
    dateRange: Pair<Long?, Long?>,
    onDateRangeChange: (Long?, Long?) -> Unit,
    onDeletePurchase: (EquipmentPurchase) -> Unit,
    onRecordPurchase: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat(FMT_DATE, Locale.getDefault()) }
    val equipmentMap = remember(equipmentList) { equipmentList.associateBy { it.id } }

    var deleteTarget by remember { mutableStateOf<EquipmentPurchase?>(null) }

    if (deleteTarget != null) {
        DasurvConfirmDialog(
            onDismissRequest = { deleteTarget = null },
            icon = Icons.Default.Delete,
            title = "Delete Purchase",
            message = "Remove this purchase record?",
            onConfirm = {
                onDeletePurchase(deleteTarget!!)
                deleteTarget = null
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 32.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = M3SurfaceContainer,
            tonalElevation = 0.dp
        ) {
            Scaffold(
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { DasurvTopAppBarTitle("Purchase History") },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, "Close")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onRecordPurchase,
                        modifier = Modifier.navigationBarsPadding(),
                        containerColor = M3CyanColor,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, "Record Purchase")
                    }
                },
                containerColor = M3SurfaceContainer
            ) { padding ->
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date range filter
                    item {
                        M3ListCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Filter by Date", style = M3LabelStyle)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    DasurvClickableField(
                                        label = "From",
                                        value = dateRange.first?.let { dateFormat.format(Date(it)) } ?: "",
                                        placeholder = "Any",
                                        onClick = {
                                            showDatePickerStartOfDay(context, dateRange.first ?: System.currentTimeMillis()) {
                                                onDateRangeChange(it, dateRange.second)
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
                                                onDateRangeChange(dateRange.first, it)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (dateRange.first != null || dateRange.second != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { onDateRangeChange(null, null) }) {
                                        Text("Clear Filter", color = M3CyanColor)
                                    }
                                }
                            }
                        }
                    }

                    // Purchase list or empty state
                    if (purchases.isEmpty()) {
                        item {
                            DasurvEmptyState(
                                icon = Icons.Default.ShoppingCart,
                                message = "No purchases recorded yet"
                            )
                        }
                    } else {
                        item {
                            M3ListCard {
                                purchases.forEachIndexed { index, purchase ->
                                    val eqName = equipmentMap[purchase.equipmentId]?.name ?: "Unknown"
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
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
                                    if (index < purchases.lastIndex) {
                                        M3ListDivider()
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.navigationBarsPadding().height(72.dp)) }
                }
            }
        }
    }
}
