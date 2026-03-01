package com.dasurv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Primary.copy(alpha = 0.12f),
    secondary = Success,
    onSecondary = Color.White,
    tertiary = Warning,
    error = Danger,
    onError = Color.White,
    background = Gray50,
    onBackground = Gray800,
    surface = Color.White,
    onSurface = Gray800,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray300,
    outlineVariant = Gray200,
)

@Composable
fun DasurvTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = DasurvTypography,
            shapes = DasurvShapes,
            content = content,
        )
    }
}

object DasurvTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}
