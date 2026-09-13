package com.preciousmetals.tracker.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.preciousmetals.tracker.ui.theme.BlackEmber
import kotlin.math.roundToInt

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val PillOuterMargin = 22.dp
private val PillHeight = 72.dp // 16dp vertical padding on each side + 40dp tab height

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
 * The colored pill is a single element, drawn once behind the [Row] of tabs (not owned by any
 * individual tab). Each tab's real on-screen bounds are measured every layout pass via
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
            .padding(bottom = PillOuterMargin)
            .clip(CircleShape)
            .background(BlackEmber)
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
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                NavPill(
                    tab = tab,
                    selected = isSelected(tab),
                    onClick = { onSelect(tab) },
                    modifier = Modifier.onGloballyPositioned { coords ->
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
    return navigationBarInset + PillOuterMargin + PillHeight + gap
}

/** Bottom padding a scrollable screen needs so its last item can fully clear the floating pill nav. */
@Composable
fun bottomNavContentPadding(): Dp = bottomNavClearance(gap = 16.dp)

/** Bottom offset for a transient overlay (snackbar) that should sit flush against the pill nav. */
@Composable
fun bottomNavOverlayPadding(): Dp = bottomNavClearance(gap = 0.dp)

/**
 * A tab's icon (plus label once selected) with no background of its own — the shared sliding pill
 * behind it is drawn by the parent (see [FloatingBottomNav]). Layout here (padding, label
 * presence) changes instantly on selection; only the icon's color and scale get their own quick,
 * independent micro-animation, decoupled from the pill's slide.
 */
@Composable
private fun NavPill(tab: BottomTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.33f),
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
            .height(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = if (selected) 16.dp else 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp).scale(iconScale)) {
            Icon(imageVector = tab.icon, contentDescription = tab.label, tint = contentColor)
        }
        if (selected) {
            Text(
                text = tab.label,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}
