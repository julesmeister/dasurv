package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.ui.component.DateNavigationBar
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvConfirmDialog
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvSummaryStrip
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3GreenColor
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3Outline
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.component.rememberSnackbarState
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import com.dasurv.util.showDatePicker
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTransactionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadAllClients() }

    val context = LocalContext.current
    val spacing = DasurvTheme.spacing

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()
    val allTimeSummary by viewModel.allTimeSummary.collectAsStateWithLifecycle()
    val formattedMonth by viewModel.formattedMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    var transactionToDelete by remember { mutableStateOf<ClientTransaction?>(null) }
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = rememberSnackbarState(snackbarMsg, viewModel::clearSnackbar)

    val clientMap = remember(clients) { clients.associateBy { it.id } }

    val runningBalances = remember(transactions) {
        val sorted = transactions.sortedBy { it.date }
        val balances = mutableMapOf<Long, Double>()
        var running = 0.0
        for (tx in sorted) {
            running += tx.amount
            balances[tx.id] = running
        }
        balances
    }

    if (transactionToDelete != null) {
        DasurvConfirmDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = Icons.Default.Delete,
            title = "Delete Transaction",
            message = "Are you sure you want to delete this transaction?",
            onConfirm = {
                viewModel.deleteTransaction(transactionToDelete!!)
                transactionToDelete = null
            }
        )
    }

    val balanceColor = if (allTimeSummary.balance > 0.01) M3RedColor else M3Primary

    Scaffold(
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("All Transactions") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(M3SurfaceContainer)
        ) {
            // Date navigation
            DateNavigationBar(
                title = formattedMonth,
                subtitle = "${transactions.size} transaction${if (transactions.size != 1) "s" else ""}",
                accentColor = M3GreenColor,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() },
                onCenterClick = {
                    showDatePicker(context, selectedDate.time) { millis ->
                        viewModel.setDate(Date(millis))
                    }
                }
            )
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // Monthly charged / paid
            DasurvSummaryStrip(
                label = "Charged",
                value = "\u20B1${monthSummary.totalCharged.formatCurrency()}",
                accentColor = M3GreenColor,
                secondaryLabel = "Paid",
                secondaryValue = "\u20B1${monthSummary.totalPaid.formatCurrency()}",
            )
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // All-time balance
            DasurvSummaryStrip(
                label = if (allTimeSummary.balance > 0.01) "Total Outstanding" else "Total Balance",
                value = "\u20B1${allTimeSummary.balance.formatCurrency()}",
                accentColor = balanceColor,
                secondaryLabel = "All-time Charged",
                secondaryValue = "\u20B1${allTimeSummary.totalCharged.formatCurrency()}",
            )
            HorizontalDivider(color = M3Outline, thickness = 1.dp)

            // List or empty
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DasurvEmptyState(
                        icon = Icons.Default.Receipt,
                        message = "No transactions in $formattedMonth",
                        action = {
                            TextButton(onClick = { viewModel.goToLatestTransaction() }) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = M3GreenColor,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Go to latest transaction",
                                    color = M3GreenColor,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                    items(transactions.size, key = { transactions[it].id }) { index ->
                        val tx = transactions[index]
                        val clientName = clientMap[tx.clientId]?.name ?: "Unknown"
                        Column(modifier = Modifier.background(Color.White)) {
                            Text(
                                text = clientName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = M3Primary,
                                modifier = Modifier.padding(start = spacing.lg, top = spacing.sm)
                            )
                            TransactionRow(
                                transaction = tx,
                                runningBalance = runningBalances[tx.id] ?: 0.0,
                                onDelete = { transactionToDelete = tx }
                            )
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
