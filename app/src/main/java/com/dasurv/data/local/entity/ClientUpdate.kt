package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object UpdateTags {
    val OUTCOME = listOf("patchy", "even", "crisp", "faded", "overworked", "saturated", "migrated")
    val HEALING = listOf("swollen", "peeling", "healed well", "scarring", "dry", "flaky")
    val ACTION = listOf("needs touch-up", "no follow-up needed", "schedule follow-up")
    val ALL = OUTCOME + HEALING + ACTION

    fun categoryOf(tag: String): String = when (tag) {
        in OUTCOME -> "outcome"
        in HEALING -> "healing"
        in ACTION -> "action"
        else -> "custom"
    }
}

@Stable
@Entity(
    tableName = "client_updates",
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
        Index(value = ["sessionId"]),
        Index(value = ["date"])
    ]
)
data class ClientUpdate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val sessionId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val photoUri: String? = null,
    val tags: String = "[]",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
