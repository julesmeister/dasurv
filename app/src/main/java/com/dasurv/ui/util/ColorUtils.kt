package com.dasurv.ui.util

import androidx.compose.ui.graphics.Color

fun parseHexSafe(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
