package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class LipZone {
    UPPER, LOWER
}

@Stable
@Entity(
    tableName = "lip_photo_pigments",
    foreignKeys = [
        ForeignKey(
            entity = LipPhoto::class,
            parentColumns = ["id"],
            childColumns = ["lipPhotoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["lipPhotoId"])
    ]
)
data class LipPhotoPigment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lipPhotoId: Long,
    val lipZone: LipZone,
    val pigmentName: String,
    val pigmentBrand: String,
    val pigmentColorHex: String,
    val isRecommended: Boolean = false,
    val notes: String = ""
)
