package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.preciousmetals.tracker.ui.theme.BlackEmber
import com.preciousmetals.tracker.ui.theme.DeepBrown
import com.preciousmetals.tracker.ui.theme.EmberOrange
import com.preciousmetals.tracker.ui.theme.NearBlackEmber
import com.preciousmetals.tracker.ui.theme.RustBrown
import kotlin.math.hypot
import kotlin.math.max

/**
 * The app-wide "braise" background: a single radial gradient painted once behind the whole
 * navigation graph (not re-painted per screen), so every screen shares the exact same ember,
 * without seams between transparent Scaffolds. Reproduces, verbatim, the redesign report's CSS:
 * radial-gradient(circle at 15% 50%, #8a3410 0%, #5c220c 22%, #2c1108 44%, #100907 68%, #070504 100%)
 */
@Composable
fun EmberGradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val center = Offset(size.width * 0.15f, size.height * 0.5f)
                val corners = listOf(
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    Offset(0f, size.height),
                    Offset(size.width, size.height),
                )
                val radius = corners.maxOf { hypot((it.x - center.x), (it.y - center.y)) }
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.00f to EmberOrange,
                            0.22f to RustBrown,
                            0.44f to DeepBrown,
                            0.68f to NearBlackEmber,
                            1.00f to BlackEmber,
                        ),
                        center = center,
                        radius = max(radius, 1f),
                    ),
                )
            },
        content = content,
    )
}
