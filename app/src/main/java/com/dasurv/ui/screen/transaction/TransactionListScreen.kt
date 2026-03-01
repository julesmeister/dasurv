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
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.DasurvAddFab
import com.dasurv.ui.component.DasurvBackButton
import com.dasurv.ui.component.DasurvConfirmDialog
import com.dasurv.ui.component.DasurvEmptyState
import com.dasurv.ui.component.DasurvTopAppBarTitle
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3AmberContainer
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
import com.dasurv.ui.theme.DasurvTheme
import java.text.SimpleDateFormat
import java.util.*

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
                                    "$${String.format("%.2f", summary.balance)}",
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
                                color = Color(0xFFF0F1FA)
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
                                        "$${String.format("%.2f", summary.totalCharged)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = M3OnSurface
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(spacing.md),
                                color = Color(0xFFF0F1FA)
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
                                        "$${String.format("%.2f", summary.totalPaid)}",
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

@Composable
private fun TransactionRow(
    transaction: ClientTransaction,
    runningBalance: Double,
    onDelete: () -> Unit
) {
    val spacing = DasurvTheme.spacing
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val isCharge = transaction.type == TransactionType.CHARGE || transaction.type == TransactionType.REFUND

    // Friendly display names
    val typeLabel = when (transaction.type) {
        TransactionType.CHARGE -> "Charge"
        TransactionType.PAYMENT -> "Payment"
        TransactionType.DEPOSIT -> "Deposit"
        TransactionType.TIP -> "Tip"
        TransactionType.REFUND -> "Refund"
    }
    val methodLabel = transaction.paymentMethod?.let {
        when (it) {
            com.dasurv.data.local.entity.PaymentMethod.CASH -> "Cash"
            com.dasurv.data.local.entity.PaymentMethod.CARD -> "Card"
            com.dasurv.data.local.entity.PaymentMethod.E_TRANSFER -> "E-transfer"
            com.dasurv.data.local.entity.PaymentMethod.OTHER -> "Other"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type icon indicator
        Surface(
            shape = RoundedCornerShape(spacing.md),
            color = if (isCharge) M3RedContainer else M3AmberContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isCharge) "+" else "-",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCharge) M3RedColor else M3AmberColor
                )
            }
        }

        Spacer(modifier = Modifier.width(spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = M3OnSurface
                )
                if (methodLabel != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF0F1FA)
                    ) {
                        Text(
                            text = methodLabel,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = M3OnSurfaceVariant
                        )
                    }
                }
            }
            Text(
                text = dateFormat.format(Date(transaction.date)),
                style = MaterialTheme.typography.bodySmall,
                color = M3OnSurfaceVariant
            )
            if (transaction.notes.isNotBlank()) {
                Text(
                    text = transaction.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = M3OnSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (transaction.amount > 0) "+" else ""}$${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCharge) M3RedColor else M3AmberColor
            )
            Text(
                text = "Bal: $${String.format("%.2f", runningBalance)}",
                style = MaterialTheme.typography.labelSmall,
                color = M3OnSurfaceVariant
            )
            // Only allow deleting non-CHARGE transactions
            if (transaction.type != TransactionType.CHARGE) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = M3OnSurfaceVariant
                    )
                }
            }
        }
    }
}
