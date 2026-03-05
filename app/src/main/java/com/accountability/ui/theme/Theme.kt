package com.accountability.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ─── Light: Washi paper, matcha ink ──────────────────
private val LightColorScheme = lightColorScheme(
    primary = MatchaGreen,
    onPrimary = WashiPaper,
    primaryContainer = MatchaGreenLight,
    onPrimaryContainer = SumiInk,
    secondary = SakuraBlush,
    onSecondary = SumiInk,
    secondaryContainer = SakuraBlush.copy(alpha = 0.3f),
    onSecondaryContainer = SumiInk,
    tertiary = AgedCopper,
    onTertiary = WashiPaper,
    tertiaryContainer = AgedCopperLight.copy(alpha = 0.3f),
    onTertiaryContainer = SumiInk,
    background = WashiPaper,
    onBackground = SumiInk,
    surface = WashiPaper,
    onSurface = SumiInk,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = SumiInkSoft,
    outline = HairlineBorder,
    outlineVariant = HairlineBorder,
    error = ErrorRust,
    onError = WashiPaper,
)

// ─── Dark: Deep ink night, warm tones ────────────────
private val DarkColorScheme = darkColorScheme(
    primary = MatchaGreenLight,
    onPrimary = DeepInk,
    primaryContainer = MatchaGreenDark,
    onPrimaryContainer = WashiText,
    secondary = SakuraBlushDark,
    onSecondary = DeepInk,
    secondaryContainer = SakuraBlushDark.copy(alpha = 0.2f),
    onSecondaryContainer = WashiText,
    tertiary = AgedCopperLight,
    onTertiary = DeepInk,
    tertiaryContainer = AgedCopper.copy(alpha = 0.2f),
    onTertiaryContainer = WashiText,
    background = DeepInk,
    onBackground = WashiText,
    surface = SurfaceDark,
    onSurface = WashiText,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = WashiTextDim,
    outline = HairlineBorderDark,
    outlineVariant = HairlineBorderDark,
    error = ErrorRustLight,
    onError = DeepInk,
)

// ─── Shapes: Gentle, not too rounded ─────────────────
val ZenShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun AccountabilityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ZenTypography,
        shapes = ZenShapes,
        content = content
    )
}
