package com.dasurv.data.model

data class CostSummary(
    val items: List<CostItem>,
    val totalCost: Double
)

data class CostItem(
    val name: String,
    val category: String,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double = quantity * unitCost,
    val perPieceInfo: String = ""
)
