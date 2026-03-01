package com.dasurv.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Pigment(
    val name: String,
    val brand: PigmentBrand,
    val colorHex: String,
    val undertone: String = "", // warm, cool, neutral
    val intensity: String = "", // light, medium, dark
    val description: String = "",
    val retentionRate: Float = 0.55f,   // 0.0-1.0, fraction retained after healing
    val healingNotes: String = ""        // per-pigment healing behavior
)
