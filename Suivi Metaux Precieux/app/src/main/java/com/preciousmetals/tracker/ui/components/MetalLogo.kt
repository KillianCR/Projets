package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.Icon
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
import com.preciousmetals.tracker.ui.theme.GoldGradientDeep
import com.preciousmetals.tracker.ui.theme.GoldGradientLight
import com.preciousmetals.tracker.ui.theme.GoldGradientMid
import com.preciousmetals.tracker.ui.theme.SilverGradientDeep
import com.preciousmetals.tracker.ui.theme.SilverGradientLight
import com.preciousmetals.tracker.ui.theme.SilverGradientMid
import com.preciousmetals.tracker.ui.theme.brandColor

/**
 * A small coin-style "logo" for a metal, no external asset needed. Or/Argent get a realistic
 * metallic-sheen gradient circle with a dark "$" glyph (report: "Logos or / argent" — replacing
 * the previous flat tinted pastilles); Platine/Palladium keep the flat tinted-icon treatment,
 * which the redesign report never addressed.
 */
@Composable
fun MetalLogo(metal: Metal, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    when (metal) {
        Metal.GOLD -> MetallicLogo(size, modifier, GoldGradientLight, GoldGradientMid, GoldGradientDeep)
        Metal.SILVER -> MetallicLogo(size, modifier, SilverGradientLight, SilverGradientMid, SilverGradientDeep)
        Metal.PLATINUM, Metal.PALLADIUM -> {
            val color = metal.brandColor()
            Box(
                modifier = modifier
                    .size(size)
                    .background(color.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = metal.displayNameFr,
                    tint = color,
                    modifier = Modifier.size(size * 0.55f),
                )
            }
        }
    }
}

@Composable
private fun MetallicLogo(size: Dp, modifier: Modifier, light: androidx.compose.ui.graphics.Color, mid: androidx.compose.ui.graphics.Color, deep: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = modifier
            .size(size)
            .background(Brush.linearGradient(listOf(light, mid, deep)), CircleShape),
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
