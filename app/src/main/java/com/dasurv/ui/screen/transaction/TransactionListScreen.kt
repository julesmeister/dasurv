package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
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
import com.dasurv.data.local.entity.ClientTransaction
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
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer
import com.dasurv.ui.component.M3SnackbarHost
import com.dasurv.ui.component.M3SurfaceContainer
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    LaunchedEffect(clientId) { viewModel.loadClient(clientId) }

    val spacing = DasurvTheme.spacing

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    var transactionToDelete by remember { mutableStateOf<ClientTransaction?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    // Compute running balances (accumulate from oldest to newest, display newest first)
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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = M3SurfaceContainer,
        snackbarHost = { M3SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { DasurvTopAppBarTitle("Transactions") },
                navigationIcon = { DasurvBackButton(onClick = onNavigateBack) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            DasurvAddFab(
                onClick = { onNavigateToAddTransaction(clientId) },
                contentDescription = "Add Transaction"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            // Balance summary header
            item {
                M3ListCard {
                    Column(modifier = Modifier.padding(spacing.xl)) {
                        // Balance hero
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(spacing.lg),
                            color = if (summary.balance > 0.01)
                                M3RedContainer
                            else M3PrimaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (summary.balance > 0.01) "Outstanding Balance" else "Balance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (summary.balance > 0.01)
                                        M3RedColor
                                    else M3Primary
                                )
                                Spacer(modifier = Modifier.height(spacing.xs))
                                Text(
                                    "$${summary.balance.formatCurrency()}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.balance > 0.01)
                                        M3RedColor
                                    else M3Primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(spacing.md))

                        // Charged / Paid stat pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(spacing.md),
                                color = M3FieldBg
                            ) {
                                Column(
                                    modifier = Modifier.padding(spacing.md),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Charged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = M3OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$${summary.totalCharged.formatCurrency()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3OnSurface
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(spacing.md),
                                color = M3FieldBg
                            ) {
                                Column(
                                    modifier = Modifier.padding(spacing.md),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Paid",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = M3OnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$${summary.totalPaid.formatCurrency()}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3OnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    DasurvEmptyState(
                        icon = Icons.Default.Receipt,
                        message = "No transactions yet"
                    )
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Text(
                        "All Transactions (${transactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = M3OnSurface,
                        modifier = Modifier.padding(horizontal = spacing.lg)
                    )
                }

                item {
                    M3ListCard {
                        transactions.forEachIndexed { index, tx ->
                            TransactionRow(
                                transaction = tx,
                                runningBalance = runningBalances[tx.id] ?: 0.0,
                                onDelete = { transactionToDelete = tx }
                            )
                            if (index < transactions.lastIndex) {
                                M3ListDivider()
                            }
                        }
                    }
                }
            }

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}
