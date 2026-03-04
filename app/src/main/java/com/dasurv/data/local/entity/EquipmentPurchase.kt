package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "equipment_purchases",
    foreignKeys = [
        ForeignKey(
            entity = Equipment::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("equipmentId"), Index("purchaseDate")]
)
data class EquipmentPurchase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipmentId: Long,
    val quantity: Int,
    val totalCost: Double = 0.0,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val purchaseSource: String = "",
    val seller: String = ""
)
