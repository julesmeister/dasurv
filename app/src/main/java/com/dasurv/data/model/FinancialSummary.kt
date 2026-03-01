package com.dasurv.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class FinancialSummary(
    val totalCharged: Double = 0.0,
    val totalPaid: Double = 0.0,
    val balance: Double = 0.0
)
