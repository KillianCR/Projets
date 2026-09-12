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
 * Eased (quadratic-ish) alpha curve for the fade, as (position, alpha) pairs from fully
 * transparent to fully opaque. A plain 2-stop linear gradient still reaches alpha 0 at its very
 * first pixel mathematically, but perceptually it ramps up so fast that content looks like it's
 * still ~15% visible right up until the edge — this curve keeps alpha low for most of the band and
 * only rises steeply at the very end, so the fade genuinely reads as reaching 0% instead of
 * appearing to stop partway through.
 */
private val fadeCurve = listOf(0f to 0f, 0.2f to 0.04f, 0.4f to 0.16f, 0.6f to 0.36f, 0.8f to 0.64f, 1f to 1f)

/**
 * Fades a horizontally scrollable row's content to transparent at its left/right edges instead
 * of clipping it with a hard line — a hint that there's more to scroll to, for every chip/card
 * row in the app (metal pickers, purity pickers, the metal-price ticker cards). Each edge only
 * fades when [listState] says there's actually more content that way — the first/last item stays
 * fully opaque once scrolled flush against that edge, since there's nothing left to hint at.
 */
fun Modifier.horizontalFadingEdges(listState: LazyListState, edgeWidth: Dp = 24.dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fraction = (edgeWidth.toPx() / size.width).coerceIn(0f, 0.5f)
        val stops = mutableListOf<Pair<Float, Color>>()
        if (listState.canScrollBackward) {
            fadeCurve.forEach { (t, a) -> stops += (t * fraction) to Color.Black.copy(alpha = a) }
        } else {
            stops += 0f to Color.Black
        }
        if (listState.canScrollForward) {
            fadeCurve.forEach { (t, a) -> stops += (1f - t * fraction) to Color.Black.copy(alpha = a) }
        } else {
            stops += 1f to Color.Black
        }
        drawRect(
            brush = Brush.horizontalGradient(*stops.toTypedArray()),
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
fun Modifier.topFadingEdge(scrollableState: ScrollableState, edgeHeight: Dp = 32.dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        if (scrollableState.canScrollBackward) {
            drawRect(
                brush = Brush.verticalGradient(
                    *fadeCurve.map { (t, a) -> t to Color.Black.copy(alpha = a) }.toTypedArray(),
                    startY = 0f,
                    endY = edgeHeight.toPx(),
                ),
                blendMode = BlendMode.DstIn,
            )
        }
    }
