package com.dasurv.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CaptureType {
    BEFORE, AFTER, FOLLOW_UP
}

@Entity(
    tableName = "lip_photos",
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
        Index(value = ["capturedAt"])
    ]
)
data class LipPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val photoUri: String,
    val captureType: CaptureType,
    val followUpInterval: String? = null,
    val capturedAt: Long = System.currentTimeMillis(),
    val upperLipColorHex: String? = null,
    val upperLipCategory: String? = null,
    val upperLipHue: Float? = null,
    val upperLipSaturation: Float? = null,
    val upperLipValue: Float? = null,
    val lowerLipColorHex: String? = null,
    val lowerLipCategory: String? = null,
    val lowerLipHue: Float? = null,
    val lowerLipSaturation: Float? = null,
    val lowerLipValue: Float? = null,
    val notes: String = "",
    val upperLipScale: Float = 1.0f,
    val lowerLipScale: Float = 1.0f
)
