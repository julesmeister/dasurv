package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "equipment",
    indices = [Index("category"), Index("type")]
)
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "", // pigment, needle, numbing, other
    val brand: String = "",
    val costPerUnit: Double = 0.0,
    val unitsPerSession: Double = 1.0,
    val stockQuantity: Int = 0,
    val notes: String = "",
    val type: String = "consumable", // "studio" or "consumable"
    val piecesPerPackage: Int = 1,
    val purchaseSource: String = "",  // app/platform (e.g. Shopee, Lazada)
    val seller: String = ""           // seller name within the platform
) {
    @get:Ignore
    val costPerPiece: Double
        get() = if (piecesPerPackage > 0) costPerUnit / piecesPerPackage else 0.0
}
