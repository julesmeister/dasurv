package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Stable
@Entity(
    tableName = "session_template_equipment",
    foreignKeys = [
        ForeignKey(
            entity = SessionTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Equipment::class,
            parentColumns = ["id"],
            childColumns = ["equipmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("templateId"), Index("equipmentId")]
)
data class SessionTemplateEquipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val equipmentId: Long,
    val quantity: Int = 1
)
