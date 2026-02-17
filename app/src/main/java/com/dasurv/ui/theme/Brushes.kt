package com.dasurv.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val GlassBrush = Brush.verticalGradient(
    colors = listOf(
        RoseDark.copy(alpha = 0.55f),
        Color.Black.copy(alpha = 0.65f)
    )
)
