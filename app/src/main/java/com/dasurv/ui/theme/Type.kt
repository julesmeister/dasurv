package com.dasurv.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography scale
val TypographyXs = TextStyle(fontSize = 11.sp, lineHeight = 16.sp)
val TypographySm = TextStyle(fontSize = 12.sp, lineHeight = 18.sp)
val TypographyBase = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
val TypographyLg = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
val TypographyXl = TextStyle(fontSize = 18.sp, lineHeight = 28.sp)
val TypographyXxl = TextStyle(fontSize = 20.sp, lineHeight = 28.sp)
val TypographyXxxl = TextStyle(fontSize = 36.sp, lineHeight = 40.sp)

val DasurvTypography = Typography(
    displayLarge = TypographyXxxl.copy(fontWeight = FontWeight.Bold, color = Gray800),
    headlineLarge = TypographyXxl.copy(fontWeight = FontWeight.Bold, color = Gray800),
    headlineMedium = TypographyXl.copy(fontWeight = FontWeight.SemiBold, color = Gray800),
    titleLarge = TypographyLg.copy(fontWeight = FontWeight.SemiBold, color = Gray800),
    titleMedium = TypographyBase.copy(fontWeight = FontWeight.SemiBold, color = Gray700),
    bodyLarge = TypographyBase.copy(color = Gray700),
    bodyMedium = TypographySm.copy(color = Gray600),
    bodySmall = TypographyXs.copy(color = Gray500),
    labelLarge = TypographyBase.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = TypographySm.copy(fontWeight = FontWeight.Medium),
    labelSmall = TypographyXs.copy(fontWeight = FontWeight.Medium),
)
