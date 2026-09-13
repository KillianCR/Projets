package com.preciousmetals.tracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.preciousmetals.tracker.domain.model.Metal

/** The validated categorical color for [metal] (see [MetalColors]) — the app has a single theme. */
fun Metal.brandColor(): Color = when (this) {
    Metal.GOLD -> MetalColors.gold
    Metal.SILVER -> MetalColors.silver
    Metal.PLATINUM -> MetalColors.platinum
    Metal.PALLADIUM -> MetalColors.palladium
    Metal.COPPER -> MetalColors.copper
}
