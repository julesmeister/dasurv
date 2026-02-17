package com.dasurv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId")]
)
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val date: Long = System.currentTimeMillis(),
    val procedure: String = "",
    val notes: String = "",
    val lipColorCategory: String? = null,
    val lipColorHex: String? = null,
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val totalCost: Double = 0.0,
    val durationSeconds: Long = 0,
    val upperLipSeconds: Long = 0,
    val lowerLipSeconds: Long = 0
)
