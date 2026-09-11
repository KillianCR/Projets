package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.theme.BlackEmber
import com.preciousmetals.tracker.ui.theme.CopperDotDeep
import com.preciousmetals.tracker.ui.theme.CopperDotLight
import com.preciousmetals.tracker.ui.theme.GoldGradientDeep
import com.preciousmetals.tracker.ui.theme.GoldGradientLight
import com.preciousmetals.tracker.ui.theme.GoldGradientMid
import com.preciousmetals.tracker.ui.theme.PalladiumDotDeep
import com.preciousmetals.tracker.ui.theme.PalladiumDotLight
import com.preciousmetals.tracker.ui.theme.PlatinumDotDeep
import com.preciousmetals.tracker.ui.theme.PlatinumDotLight
import com.preciousmetals.tracker.ui.theme.SilverGradientDeep
import com.preciousmetals.tracker.ui.theme.SilverGradientLight
import com.preciousmetals.tracker.ui.theme.SilverGradientMid

/**
 * A small coin-style "logo" for a metal, no external asset needed: a metallic-sheen gradient
 * circle with a dark "$" glyph, same gradient stops used for each metal's chip dot (MetalBadge).
 */
@Composable
fun MetalLogo(metal: Metal, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val colors = when (metal) {
        Metal.GOLD -> listOf(GoldGradientLight, GoldGradientMid, GoldGradientDeep)
        Metal.SILVER -> listOf(SilverGradientLight, SilverGradientMid, SilverGradientDeep)
        Metal.PLATINUM -> listOf(PlatinumDotLight, PlatinumDotDeep)
        Metal.PALLADIUM -> listOf(PalladiumDotLight, PalladiumDotDeep)
        Metal.COPPER -> listOf(CopperDotLight, CopperDotDeep)
    }
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(colors), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$",
            color = BlackEmber,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
