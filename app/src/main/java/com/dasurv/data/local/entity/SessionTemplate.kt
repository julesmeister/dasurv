package com.dasurv.data.local.entity

import androidx.compose.runtime.Stable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Stable
@Entity(tableName = "session_templates")
data class SessionTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val procedure: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
