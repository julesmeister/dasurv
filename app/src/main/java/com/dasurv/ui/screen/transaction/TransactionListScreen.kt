package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.DasurvConfirmDialog
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

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    var transactionToDelete by remember { mutableStateOf<ClientTransaction?>(null) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToAddTransaction(clientId) },
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Balance summary header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Balance hero
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (summary.balance > 0.01)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    if (summary.balance > 0.01) "Outstanding Balance" else "Balance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (summary.balance > 0.01)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "$${String.format("%.2f", summary.balance)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.balance > 0.01)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Charged / Paid stat pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Charged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$${String.format("%.2f", summary.totalCharged)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Paid",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$${String.format("%.2f", summary.totalPaid)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "All Transactions (${transactions.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(transactions, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        runningBalance = runningBalances[tx.id] ?: 0.0,
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }

            // Bottom spacer for FAB
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: ClientTransaction,
    runningBalance: Double,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isCharge)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isCharge) "+" else "-",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCharge)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (methodLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = methodLabel,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = dateFormat.format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.amount > 0) "+" else ""}$${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCharge) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Bal: $${String.format("%.2f", runningBalance)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
