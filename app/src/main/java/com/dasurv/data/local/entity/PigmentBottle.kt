package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "pigment_bottles",
    indices = [Index("remainingMl"), Index("pigmentName", "pigmentBrand")]
)
data class PigmentBottle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pigmentName: String,
    val pigmentBrand: String,
    val colorHex: String,
    val isCustom: Boolean = false,
    val bottleSizeMl: Double = 15.0,
    val remainingMl: Double = 15.0,
    val pricePerBottle: Double = 0.0,
    val pricePerMl: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val equipmentId: Long? = null
) {
    @get:Ignore
    val usedMl: Double
        get() = bottleSizeMl - remainingMl

    @get:Ignore
    val remainingValue: Double
        get() = remainingMl * pricePerMl

    @get:Ignore
    val usagePercentage: Float
        get() = if (bottleSizeMl > 0) (remainingMl / bottleSizeMl).toFloat() else 0f
}
