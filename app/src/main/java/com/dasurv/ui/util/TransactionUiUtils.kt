package com.dasurv.ui.util

import androidx.compose.ui.graphics.Color
import com.dasurv.data.local.entity.PaymentMethod
import com.dasurv.data.local.entity.TransactionType
import com.dasurv.ui.component.*

fun TransactionType.displayName(): String = when (this) {
    TransactionType.CHARGE -> "Charge"
    TransactionType.PAYMENT -> "Payment"
    TransactionType.DEPOSIT -> "Deposit"
    TransactionType.TIP -> "Tip"
    TransactionType.REFUND -> "Refund"
}

fun TransactionType.isDebit(): Boolean =
    this == TransactionType.CHARGE || this == TransactionType.REFUND

fun TransactionType.color(): Color = if (isDebit()) M3RedColor else M3AmberColor

fun TransactionType.containerColor(): Color = if (isDebit()) M3RedContainer else M3AmberContainer

fun PaymentMethod.displayName(): String = when (this) {
    PaymentMethod.CASH -> "Cash"
    PaymentMethod.CARD -> "Card"
    PaymentMethod.E_TRANSFER -> "E-transfer"
    PaymentMethod.OTHER -> "Other"
}
