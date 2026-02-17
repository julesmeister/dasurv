package com.dasurv.data.model

data class LipColorAnalysis(
    val dominantColorHex: String,
    val category: LipColorCategory,
    val hue: Float,
    val saturation: Float,
    val value: Float
)

enum class LipColorCategory(val displayName: String) {
    PALE_PINK("Pale Pink"),
    LIGHT_PINK("Light Pink"),
    MEDIUM_PINK("Medium Pink"),
    ROSE("Rose"),
    MAUVE("Mauve"),
    DARK_PINK("Dark Pink"),
    RED("Red"),
    DARK_RED("Dark Red"),
    BROWN("Brown"),
    PIGMENTED("Highly Pigmented"),
    PALE("Pale/Colorless")
}

data class DualLipAnalysis(
    val upperLip: LipColorAnalysis?,
    val lowerLip: LipColorAnalysis?
)
