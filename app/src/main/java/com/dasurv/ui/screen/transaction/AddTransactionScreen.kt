package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.PaymentMethod
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.DasurvFormCard
import com.dasurv.ui.component.DasurvFormScaffold
import com.dasurv.ui.component.FormDefaults
import com.dasurv.ui.component.FormRow
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3OnSurface
import com.dasurv.ui.component.M3OnSurfaceVariant
import com.dasurv.ui.theme.DasurvTheme

@Composable
fun AddTransactionScreen(
    clientId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    LaunchedEffect(clientId) { viewModel.loadClient(clientId) }

    val spacing = DasurvTheme.spacing
    var selectedType by remember { mutableStateOf(TransactionType.PAYMENT) }
    var amountText by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var notes by remember { mutableStateOf("") }

    val showPaymentMethod = selectedType in listOf(
        TransactionType.PAYMENT, TransactionType.DEPOSIT, TransactionType.TIP
    )

    DasurvFormScaffold(
        title = "Add Transaction",
        onNavigateBack = onNavigateBack,
        saveText = "Save",
        saveEnabled = (amountText.toDoubleOrNull() ?: 0.0) > 0,
        snackbarMessage = "Transaction saved",
        onSave = { onDone ->
            val amount = amountText.toDoubleOrNull() ?: return@DasurvFormScaffold
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
            onDone()
        }
    ) {
        // Card 1: Transaction type
        DasurvFormCard {
            Column(modifier = Modifier.padding(vertical = spacing.sm)) {
                Text(
                    "Type",
                    style = FormDefaults.LabelStyle,
                    modifier = Modifier.padding(bottom = spacing.sm)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val types = listOf(
                        TransactionType.PAYMENT to "Payment",
                        TransactionType.DEPOSIT to "Deposit",
                        TransactionType.TIP to "Tip",
                        TransactionType.REFUND to "Refund"
                    )
                    types.forEach { (type, label) ->
                        FilledTonalButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (selectedType == type)
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = M3Primary,
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                )
                            else ButtonDefaults.filledTonalButtonColors(),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Card 2: Amount
        DasurvFormCard {
            FormRow(
                label = "Amount *",
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                keyboardType = KeyboardType.Decimal
            )
        }

        // Card 3: Payment method (conditional)
        if (showPaymentMethod) {
            DasurvFormCard {
                Column(modifier = Modifier.padding(vertical = spacing.sm)) {
                    Text(
                        "Payment Method",
                        style = FormDefaults.LabelStyle,
                        modifier = Modifier.padding(bottom = spacing.sm)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val methods = listOf(
                            PaymentMethod.CASH to "Cash",
                            PaymentMethod.CARD to "Card",
                            PaymentMethod.E_TRANSFER to "E-transfer",
                            PaymentMethod.OTHER to "Other"
                        )
                        methods.forEach { (method, label) ->
                            FilledTonalButton(
                                onClick = { selectedPaymentMethod = method },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = if (selectedPaymentMethod == method)
                                    ButtonDefaults.filledTonalButtonColors(
                                        containerColor = M3AmberColor,
                                        contentColor = androidx.compose.ui.graphics.Color.White
                                    )
                                else ButtonDefaults.filledTonalButtonColors(),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 10.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }

        // Card 4: Notes
        DasurvFormCard {
            FormRow(
                label = "Notes",
                value = notes,
                onValueChange = { notes = it },
                singleLine = false
            )
        }
    }
}
