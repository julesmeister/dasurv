package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UsageLipArea { UPPER, LOWER, BOTH }

@Stable
@Entity(
    tableName = "pigment_bottle_usage",
    foreignKeys = [
        ForeignKey(
            entity = PigmentBottle::class,
            parentColumns = ["id"],
            childColumns = ["bottleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bottleId"),
        Index("clientId"),
        Index("sessionId")
    ]
)
data class PigmentBottleUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bottleId: Long,
    val clientId: Long,
    val sessionId: Long? = null,
    val lipArea: UsageLipArea = UsageLipArea.BOTH,
    val mlUsed: Double,
    val costAtTimeOfUse: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
