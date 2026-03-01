package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3AmberContainer
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.component.M3RedColor
import com.dasurv.ui.component.M3RedContainer
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.theme.DasurvTheme
import com.dasurv.util.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun TransactionRow(
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
                        color = M3FieldBg
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
                text = "${if (transaction.amount > 0) "+" else ""}$${transaction.amount.formatCurrency()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCharge) M3RedColor else M3AmberColor
            )
            Text(
                text = "Bal: $${runningBalance.formatCurrency()}",
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
