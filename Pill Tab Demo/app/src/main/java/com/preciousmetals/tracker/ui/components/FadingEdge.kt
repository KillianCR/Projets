package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.gestures.ScrollableState
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

/**
 * Fades a vertically scrollable screen's content where it meets the header above it — once
 * scrolled down, the top item fades into the header's area instead of hard-clipping right at its
 * boundary. Works for both LazyColumn ([androidx.compose.foundation.lazy.LazyListState]) and a
 * plain scrolling Column ([androidx.compose.foundation.ScrollState]), since both implement
 * [ScrollableState]. Stays fully opaque once scrolled back to the very top. Must be placed
 * *before* `.verticalScroll()`/`.scrollable()` in the modifier chain (not after) — a plain
 * Column's scroll modifier offsets everything below it in the chain along with the content, which
 * would drag this fade band off-screen with the scroll instead of anchoring it to the viewport.
 */
fun Modifier.topFadingEdge(scrollableState: ScrollableState, edgeHeight: Dp = 24.dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        if (scrollableState.canScrollBackward) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black,
                    startY = 0f,
                    endY = edgeHeight.toPx(),
                ),
                blendMode = BlendMode.DstIn,
            )
        }
    }
