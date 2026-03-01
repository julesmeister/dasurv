package com.dasurv.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class CostSummary(
    val items: List<CostItem>,
    val totalCost: Double
)

@Immutable
data class CostItem(
    val name: String,
    val category: String,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double = quantity * unitCost,
    val perPieceInfo: String = ""
)
