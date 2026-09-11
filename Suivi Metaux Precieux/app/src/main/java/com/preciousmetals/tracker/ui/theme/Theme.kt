package com.preciousmetals.tracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val AppColors = darkColorScheme(
    primary = WhiteAccent,
    onPrimary = OnWhiteAccent,
    secondary = BrandGoldDark,
    onSecondary = OnBrandGold,
    background = NearBlackEmber,
    onBackground = OnBackgroundDark,
    surface = NearBlackEmber,
    onSurface = OnBackgroundDark,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = OnGlassSurfaceDark,
    outline = GlassOutlineDark,
    outlineVariant = GlassOutlineDark,
    error = NegativeRedDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = NegativeRedDark,
)

/**
 * Rounder than Material3's defaults (12dp medium) to match the pill-heavy rest of the UI
 * (34dp floating nav, 18dp quick actions) — cards and dialogs now share that same soft-rounded
 * family instead of visually belonging to a different, stricter system.
 */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/**
 * The app's one and only theme — a "braise" (ember) look per the redesign report
 * (Rapport_de_refonte___Mon_portefeuille): a radial orange-to-black gradient background,
 * translucent glass cards, and every accent unified onto a single white at varying opacities,
 * with gold kept as the secondary accent tied to the "Or" identity. Deliberately ignores the
 * device's light/dark setting and Material You wallpaper palette — the brand look never changes.
 */
@Composable
fun SuiviMetauxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
