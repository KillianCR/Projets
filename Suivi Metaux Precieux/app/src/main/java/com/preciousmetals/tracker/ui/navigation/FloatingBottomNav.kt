package com.preciousmetals.tracker.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.preciousmetals.tracker.ui.theme.CardBorderDark
import com.preciousmetals.tracker.ui.theme.CardSurfaceDark
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import kotlin.math.roundToInt

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val PillOuterMargin = 26.dp
// Smaller than PillOuterMargin: this sits on top of navigationBarsPadding(), which now (edge-to-
// edge) already reserves the real gesture/button nav bar inset — the old 22dp added there too made
// the gap below the pill look oversized.
private val PillBottomMargin = 10.dp
// 72dp -> 56dp: a bit over 20% shorter top-to-bottom.
private val PillHeight = 56.dp
// The one gap used on every side between the bar's own edge and each tab's touch/highlight area —
// same value horizontally and vertically, so the selected pastille hugs the bar identically on
// all four sides instead of reading closer on the sides than top/bottom (or vice versa).
private val PillContentInset = 4.dp
// Each tab's own touch target height — also the selected highlight's height, since it's sized off
// the same bounds. Derived from PillHeight so the vertical inset above always equals
// PillContentInset.
private val TabTouchHeight = PillHeight - PillContentInset * 2

/** The subtle rounded highlight behind the selected icon, Instagram-style — a soft light wash,
 * not a solid brand-colored pill. */
private val SelectedTabHighlight = Color.White.copy(alpha = 0.14f)

/** Strong, real backdrop blur (not just a flat translucent fill) behind the pill bar, tinted with
 * the same glass-card color as every other card in the app. backgroundColor is set explicitly
 * (every real Haze sample does, even to Color.Transparent) rather than left at the HazeStyle
 * default of Color.Unspecified — leaving it Unspecified silently produces no draw at all. */
private val PillHazeStyle = HazeStyle(
    backgroundColor = Color.Transparent,
    tints = listOf(HazeTint(CardSurfaceDark)),
    blurRadius = 30.dp,
)

/** Option A — bouncy spring: a light overshoot as the pill settles onto the new tab. */
private val PillSpringSpec: AnimationSpec<Float> =
    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)

/** Option B — fixed-duration tween: no overshoot, a steadier "precise" ease. */
private val PillTweenSpec: AnimationSpec<Float> = tween(durationMillis = 300, easing = FastOutSlowInEasing)

/** Pick whichever of the two options above feels right; swap this one line to switch. */
private val PillAnimationSpec: AnimationSpec<Float> = PillTweenSpec

/**
 * The bottom nav as a floating pill: inset from both side edges and lifted off the bottom edge
 * so the app's own content — not just its ember background — shows through around it on every
 * side. It's rendered as a plain overlay (not a Scaffold bottomBar slot, which would reserve a
 * full-width strip and dim/hide whatever screen content sits behind it), so screens scroll
 * edge-to-edge underneath it; see [bottomNavContentPadding] for the matching scroll clearance.
 *
 * The selected highlight is a single element, drawn once behind the [Row] of tabs (not owned by
 * any individual tab). Each tab's real on-screen bounds are measured every layout pass via
 * [onGloballyPositioned] and kept in [itemBounds]; the pill's target offset/width are read from
 * the currently-selected tab's bounds and animated with [animateFloatAsState] in raw pixels.
 *
 * The animated values are read inside a [Modifier.layout] block rather than passed as ordinary
 * composable parameters (as an earlier version of this did via `Modifier.width(dp)`): a value read
 * at the top of the composable forces a full recomposition on every single animation frame, which
 * is what made the slide look choppy instead of smooth. Reading `.value` inside `layout {}` only
 * triggers a re-layout of this one node each frame — no recomposition, no allocation, so the
 * animation actually runs at the full frame rate.
 */
@Composable
fun FloatingBottomNav(
    tabs: List<BottomTab>,
    isSelected: (BottomTab) -> Boolean,
    onSelect: (BottomTab) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val itemBounds = remember { mutableStateListOf<Rect>().apply { addAll(List(tabs.size) { Rect.Zero }) } }

    val selectedIndex = tabs.indexOfFirst(isSelected).coerceAtLeast(0)
    // Reading through derivedStateOf means the code below only sees a new target when the
    // SELECTED tab's own bounds change — not on every itemBounds write (e.g. an unrelated tab's
    // onGloballyPositioned firing during an unrelated layout pass).
    val selectedBounds by remember(selectedIndex) {
        derivedStateOf { itemBounds.getOrElse(selectedIndex) { Rect.Zero } }
    }

    // State objects, deliberately not `by`-delegated here: reading `.value` is deferred to the
    // layout phase inside the indicator's Modifier.layout block below, not done here at
    // composition time — see the class doc for why that split matters for smoothness.
    val animatedX = animateFloatAsState(
        targetValue = selectedBounds.left,
        animationSpec = PillAnimationSpec,
        label = "navPillOffsetX",
    )
    val animatedWidth = animateFloatAsState(
        targetValue = selectedBounds.width,
        animationSpec = PillAnimationSpec,
        label = "navPillWidth",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PillOuterMargin)
            .padding(bottom = PillBottomMargin)
            .clip(CircleShape)
            .hazeEffect(state = hazeState, style = PillHazeStyle) { blurEnabled = true }
            // Same hairline contour as the kanban / glass cards elsewhere in the app.
            .border(BorderStroke(1.dp, CardBorderDark), CircleShape)
            .onGloballyPositioned { rootCoordinates = it },
    ) {
        if (selectedBounds.width > 0f) {
            Box(
                modifier = Modifier
                    .layout { measurable, _ ->
                        val width = animatedWidth.value.roundToInt()
                        val height = selectedBounds.height.roundToInt()
                        val placeable = measurable.measure(Constraints.fixed(width, height))
                        layout(width, height) {
                            placeable.placeRelative(animatedX.value.roundToInt(), selectedBounds.top.roundToInt())
                        }
                    }
                    // Same shape as the bar itself (CircleShape), not an independent radius — on
                    // a box this small it rounds all the way, matching the bar's own fully-rounded
                    // ends.
                    .clip(CircleShape)
                    .background(SelectedTabHighlight),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Same inset on every side — each tab already claims its full weighted share (see
                // NavPill), so this is purely the gap between the pastilles and the bar's own
                // edges, not room for the highlights to grow into.
                .padding(PillContentInset),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                NavPill(
                    tab = tab,
                    selected = isSelected(tab),
                    onClick = { onSelect(tab) },
                    // Each tab claims an equal share of the bar's width (rather than wrapping just
                    // the icon) so the selected highlight reads as a wide slab close to the edges
                    // of its slot, not a small circle hugging the icon.
                    modifier = Modifier.weight(1f).onGloballyPositioned { coords ->
                        val root = rootCoordinates ?: return@onGloballyPositioned
                        val position = root.localPositionOf(coords, Offset.Zero)
                        itemBounds[index] = Rect(offset = position, size = coords.size.toSize())
                    },
                )
            }
        }
    }
}

/**
 * Bottom offset that clears the floating pill nav (its own height, the margin lifting it off the
 * edge, and the device's navigation-bar inset), plus [gap] of breathing room above it.
 */
@Composable
private fun bottomNavClearance(gap: Dp): Dp {
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return navigationBarInset + PillBottomMargin + PillHeight + gap
}

/** Bottom padding a scrollable screen needs so its last item can fully clear the floating pill nav. */
@Composable
fun bottomNavContentPadding(): Dp = bottomNavClearance(gap = 16.dp)

/** Bottom offset for a transient overlay (snackbar) that should sit flush against the pill nav. */
@Composable
fun bottomNavOverlayPadding(): Dp = bottomNavClearance(gap = 0.dp)

/**
 * A tab as a plain icon, Instagram-style — no label, each tab an equal share of the bar's width
 * (via a `weight(1f)` the caller adds to the modifier), so the shared sliding highlight behind it
 * (drawn by the parent, see [FloatingBottomNav]) settles as a wide slab close to the edges of its
 * slot, not a small circle hugging just the icon. Only the icon's color and scale animate on
 * selection.
 */
@Composable
private fun NavPill(tab: BottomTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(200),
        label = "navPillContentColor",
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "navPillIconScale",
    )

    Row(
        modifier = modifier
            .height(TabTouchHeight)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(30.dp).scale(iconScale)) {
            Icon(imageVector = tab.icon, contentDescription = tab.label, tint = contentColor)
        }
    }
}
