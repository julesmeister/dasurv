package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.PaymentMethod
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.util.displayName
import com.dasurv.ui.component.DasurvFilterChip
import com.dasurv.ui.component.DasurvCurrencyField
import com.dasurv.ui.component.DasurvFormDialog
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.component.M3LabelStyle
import com.dasurv.ui.component.M3OnSurfaceVariant

@Composable
fun TransactionFormDialog(
    clientId: Long,
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    LaunchedEffect(clientId) { viewModel.loadClient(clientId) }

    var selectedType by remember { mutableStateOf(TransactionType.PAYMENT) }
    var amountText by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val showPaymentMethod = selectedType in listOf(
        TransactionType.PAYMENT, TransactionType.DEPOSIT, TransactionType.TIP
    )

    DasurvFormDialog(
        title = "Add Transaction",
        icon = Icons.Default.Receipt,
        onDismiss = onDismiss,
        onConfirm = {
            if (!isSaving) {
                isSaving = true
                val amount = amountText.toDoubleOrNull() ?: return@DasurvFormDialog
                val signedAmount = when (selectedType) {
                    TransactionType.CHARGE, TransactionType.REFUND -> amount
                    else -> -amount
                }
                viewModel.addTransaction(
                    ClientTransaction(
                        clientId = clientId,
                        type = selectedType,
                        amount = signedAmount,
                        paymentMethod = if (showPaymentMethod) selectedPaymentMethod else null,
                        notes = notes.trim()
                    )
                )
                onDismiss()
            }
        },
        confirmLabel = "Save",
        confirmEnabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
        isLoading = isSaving,
    ) {
        // Transaction type
        Column {
            Text("Type", style = M3LabelStyle)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val types = listOf(
                    TransactionType.PAYMENT,
                    TransactionType.DEPOSIT,
                    TransactionType.TIP,
                    TransactionType.REFUND
                )
                types.forEach { type ->
                    val label = type.displayName()
                    DasurvFilterChip(
                        label = label,
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Amount
        DasurvCurrencyField(
            value = amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
            label = "Amount *"
        )

        // Payment method (conditional)
        if (showPaymentMethod) {
            Column {
                Text("Payment Method", style = M3LabelStyle)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val methods = PaymentMethod.entries
                    methods.forEach { method ->
                        val label = method.displayName()
                        val isSelected = selectedPaymentMethod == method
                        FilledTonalButton(
                            onClick = { selectedPaymentMethod = method },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) M3AmberColor.copy(alpha = 0.15f) else M3FieldBg,
                                contentColor = if (isSelected) M3AmberColor else M3OnSurfaceVariant
                            ),
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Notes
        DasurvTextField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            singleLine = false,
            minLines = 2
        )
    }
}
