package com.dasurv.ui.util

import androidx.compose.ui.graphics.Color

fun parseHexSafe(hex: String, fallback: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}
