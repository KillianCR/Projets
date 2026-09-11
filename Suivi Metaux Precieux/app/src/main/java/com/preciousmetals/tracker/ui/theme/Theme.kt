package com.preciousmetals.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = BrandGoldDark,
    onPrimary = OnBrandGold,
    secondary = BrandGoldDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
    error = NegativeRedDark,
    errorContainer = SurfaceVariantDark,
    onErrorContainer = NegativeRedDark,
)

private val LightColors = lightColorScheme(
    primary = BrandGoldLight,
    onPrimary = Color.White,
    secondary = BrandGoldLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
    error = NegativeRedLight,
    errorContainer = Color(0xFFFBEAE8),
    onErrorContainer = NegativeRedLight,
)

/**
 * The app's branded theme — a deliberate dark-first fintech look built and validated with the
 * dataviz color skill, not the device's Material You wallpaper palette. No dynamic-color option:
 * a randomly-tinted "gold" button next to hand-picked per-metal chart colors would undercut the
 * very palette this was designed around.
 */
@Composable
fun SuiviMetauxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
