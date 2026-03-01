package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "session_pigments",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SessionPigment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val pigmentName: String,
    val pigmentBrand: String,
    val drops: Int = 1,
    val costPerDrop: Double = 0.0
)
