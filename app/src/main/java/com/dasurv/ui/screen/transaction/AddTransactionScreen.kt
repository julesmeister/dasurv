package com.dasurv.ui.screen.transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dasurv.data.local.entity.ClientTransaction
import com.dasurv.data.local.entity.PaymentMethod
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.DasurvCurrencyField
import com.dasurv.ui.component.DasurvFormCard
import com.dasurv.ui.component.DasurvFormScaffold
import com.dasurv.ui.component.DasurvTextField
import com.dasurv.ui.component.M3AmberColor
import com.dasurv.ui.component.M3FieldBg
import com.dasurv.ui.component.M3Primary
import com.dasurv.ui.component.M3PrimaryContainer
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
            Column {
                Text(
                    "Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = M3OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                        val isSelected = selectedType == type
                        FilledTonalButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) M3PrimaryContainer else M3FieldBg,
                                contentColor = if (isSelected) M3Primary else M3OnSurfaceVariant
                            ),
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
            DasurvCurrencyField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = "Amount *"
            )
        }

        // Card 3: Payment method (conditional)
        if (showPaymentMethod) {
            DasurvFormCard {
                Column {
                    Text(
                        "Payment Method",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = M3OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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
        }

        // Card 4: Notes
        DasurvFormCard {
            DasurvTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                minLines = 2
            )
        }
    }
}
