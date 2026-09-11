package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.theme.CopperDotDeep
import com.preciousmetals.tracker.ui.theme.CopperDotLight
import com.preciousmetals.tracker.ui.theme.GoldGradientDeep
import com.preciousmetals.tracker.ui.theme.GoldGradientLight
import com.preciousmetals.tracker.ui.theme.PalladiumDotDeep
import com.preciousmetals.tracker.ui.theme.PalladiumDotLight
import com.preciousmetals.tracker.ui.theme.PlatinumDotDeep
import com.preciousmetals.tracker.ui.theme.PlatinumDotLight
import com.preciousmetals.tracker.ui.theme.SilverGradientDeep
import com.preciousmetals.tracker.ui.theme.SilverGradientLight

/** A small 2-stop gradient dot per metal, per the mockup's chip leading icon. */
@Composable
fun MetalBadge(metal: Metal, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    val (light, deep) = when (metal) {
        Metal.GOLD -> GoldGradientLight to GoldGradientDeep
        Metal.SILVER -> SilverGradientLight to SilverGradientDeep
        Metal.PLATINUM -> PlatinumDotLight to PlatinumDotDeep
        Metal.PALLADIUM -> PalladiumDotLight to PalladiumDotDeep
        Metal.COPPER -> CopperDotLight to CopperDotDeep
    }
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(light, deep)), CircleShape)
    )
}
