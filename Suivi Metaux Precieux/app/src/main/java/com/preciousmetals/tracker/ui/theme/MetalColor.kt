package com.preciousmetals.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.preciousmetals.tracker.domain.model.Metal

/** The validated categorical color for [metal] (see [MetalColors]), for the current theme mode. */
@Composable
fun Metal.brandColor(): Color = metalColor(this, isSystemInDarkTheme())

fun metalColor(metal: Metal, isDark: Boolean): Color = when (metal) {
    Metal.GOLD -> if (isDark) MetalColors.goldDark else MetalColors.goldLight
    Metal.SILVER -> if (isDark) MetalColors.silverDark else MetalColors.silverLight
    Metal.PLATINUM -> if (isDark) MetalColors.platinumDark else MetalColors.platinumLight
    Metal.PALLADIUM -> if (isDark) MetalColors.palladiumDark else MetalColors.palladiumLight
}
