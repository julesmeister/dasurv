package com.dasurv.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(48.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = RosePrimary,
    secondary = RoseSecondary,
    tertiary = RoseTertiary,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorColor
)

private val DarkColorScheme = darkColorScheme(
    primary = RoseTertiary,
    secondary = RoseSecondary,
    tertiary = RosePrimary,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorColor
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DasurvTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    pureBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }.let { scheme ->
        if (pureBlack && darkTheme) {
            scheme.copy(
                surface = Color.Black,
                background = Color.Black,
                surfaceContainer = Color(0xFF0A0A0A),
                surfaceContainerLow = Color.Black,
                surfaceContainerLowest = Color.Black,
                surfaceContainerHigh = Color(0xFF121212),
                surfaceContainerHighest = Color(0xFF1A1A1A)
            )
        } else {
            scheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        shapes = ExpressiveShapes,
        content = content
    )
}
