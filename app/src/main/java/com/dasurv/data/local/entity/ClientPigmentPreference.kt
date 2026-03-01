package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "client_pigment_preferences",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["clientId", "pigmentName", "pigmentBrand"], unique = true)
    ]
)
data class ClientPigmentPreference(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val pigmentName: String,
    val pigmentBrand: String,
    val pigmentColorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)
