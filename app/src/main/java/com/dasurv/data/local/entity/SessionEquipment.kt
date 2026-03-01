package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "session_equipment",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Equipment::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("equipmentId")]
)
data class SessionEquipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val equipmentId: Long,
    val quantityUsed: Double,
    val costPerPiece: Double
)
