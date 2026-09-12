package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Fades a horizontally scrollable row's content to transparent at its left/right edges instead
 * of clipping it with a hard line — a hint that there's more to scroll to, for every chip/card
 * row in the app (metal pickers, purity pickers, the metal-price ticker cards). Each edge only
 * fades when [listState] says there's actually more content that way — the first/last item stays
 * fully opaque once scrolled flush against that edge, since there's nothing left to hint at.
 */
fun Modifier.horizontalFadingEdges(listState: LazyListState, edgeWidth: Dp = 20.dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fraction = (edgeWidth.toPx() / size.width).coerceIn(0f, 0.5f)
        val startColor = if (listState.canScrollBackward) Color.Transparent else Color.Black
        val endColor = if (listState.canScrollForward) Color.Transparent else Color.Black
        drawRect(
            brush = Brush.horizontalGradient(
                0f to startColor,
                fraction to Color.Black,
                1f - fraction to Color.Black,
                1f to endColor,
            ),
            blendMode = BlendMode.DstIn,
        )
    }
