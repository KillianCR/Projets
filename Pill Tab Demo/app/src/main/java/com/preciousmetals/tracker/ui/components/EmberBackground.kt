package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 *
 * Wraps [content] in a transparent [Surface] (rather than a plain [Box]) so it still establishes
 * the theme's content color: Material3's ambient content-color default is plain black, so any
 * unstyled Text/Icon below a bare Box (no Surface/Scaffold ancestor providing one) silently
 * renders black-on-ember instead of the theme's light onBackground.
 */
@Composable
fun EmberGradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content,
        )
    }
}
