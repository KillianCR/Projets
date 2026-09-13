package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preciousmetals.tracker.domain.model.Currency
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
 * circle with a dark currency glyph ("€"/"$", matching the app's current display currency), same
 * gradient stops used for each metal's chip dot (MetalBadge).
 */
@Composable
fun MetalLogo(metal: Metal, currency: Currency, modifier: Modifier = Modifier, size: Dp = 40.dp) {
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
        val isEuro = currency == Currency.EUR
        Text(
            if (isEuro) "€" else "$",
            color = BlackEmber,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.42f).sp,
            style = MaterialTheme.typography.labelLarge,
            // The € glyph's own left-side bearing reads as visually off-center within the
            // circle (unlike $, which is symmetric) — nudge it left to compensate.
            modifier = if (isEuro) Modifier.offset(x = (-size.value * 0.03f).dp) else Modifier,
        )
    }
}

/**
 * The app's brand mark: gold, silver and copper coins cascading left-to-right, each overlapping
 * the next, gold in front — the same combined logo used as the launcher icon. Used wherever a
 * holdings list shows one logo standing in for every metal instead of each row's own [MetalLogo].
 */
@Composable
fun CombinedMetalsLogo(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val coinSize = size * 0.5f
    val step = coinSize * (36f / 84f)
    val ringColor = BlackEmber.copy(alpha = 0.55f)

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        MetalCoin(listOf(CopperDotLight, CopperDotDeep), coinSize, ringColor, Modifier.offset(x = step))
        MetalCoin(listOf(SilverGradientLight, SilverGradientMid, SilverGradientDeep), coinSize, ringColor)
        MetalCoin(listOf(GoldGradientLight, GoldGradientMid, GoldGradientDeep), coinSize, ringColor, Modifier.offset(x = -step))
    }
}

@Composable
private fun MetalCoin(colors: List<androidx.compose.ui.graphics.Color>, size: Dp, ringColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(colors), CircleShape)
            .border((size.value * 0.045f).dp, ringColor, CircleShape),
    )
}
