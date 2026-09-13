package com.preciousmetals.tracker.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.preciousmetals.tracker.ui.theme.BlackEmber
import kotlin.math.roundToInt

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val PillOuterMargin = 22.dp
private val PillHeight = 72.dp // 16dp vertical padding on each side + 40dp tab height

/**
 * The bottom nav as a floating pill: inset from both side edges and lifted off the bottom edge
 * so the app's own content — not just its ember background — shows through around it on every
 * side. It's rendered as a plain overlay (not a Scaffold bottomBar slot, which would reserve a
 * full-width strip and dim/hide whatever screen content sits behind it), so screens scroll
 * edge-to-edge underneath it; see [bottomNavContentPadding] for the matching scroll clearance.
 * The selected tab still gets a solid white pill with its icon and label, every other tab shows
 * its icon alone with no label. The colored pill itself is a single element that tracks the
 * selected tab's real (already-animating) position and size every frame, so switching tabs reads
 * as that pill physically sliding over to the new one instead of one pill vanishing and another
 * appearing in its place.
 */
@Composable
fun FloatingBottomNav(
    tabs: List<BottomTab>,
    isSelected: (BottomTab) -> Boolean,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val tabBounds = remember { mutableStateMapOf<String, Rect>() }
    val density = LocalDensity.current

    val selectedTab = tabs.firstOrNull(isSelected)
    val indicatorBounds = selectedTab?.let { tabBounds[it.route] }

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
        if (indicatorBounds != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorBounds.left.roundToInt(), indicatorBounds.top.roundToInt()) }
                    .size(with(density) { indicatorBounds.width.toDp() }, with(density) { indicatorBounds.height.toDp() })
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
            tabs.forEach { tab ->
                NavPill(
                    tab = tab,
                    selected = isSelected(tab),
                    onClick = { onSelect(tab) },
                    modifier = Modifier.onGloballyPositioned { coords ->
                        val root = rootCoordinates ?: return@onGloballyPositioned
                        val position = root.localPositionOf(coords, Offset.Zero)
                        tabBounds[tab.route] = Rect(offset = position, size = coords.size.toSize())
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
 * A tab's icon (plus label once selected) with no background of its own — the colored pill behind
 * the selected tab is drawn once by the parent (see [FloatingBottomNav]) and tracks this
 * composable's own live bounds, so its size animations here (padding, label expand/collapse) are
 * exactly what drives that shared pill's slide/resize, frame for frame.
 */
@Composable
private fun NavPill(tab: BottomTab, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.33f),
        label = "navPillContentColor",
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 16.dp else 10.dp,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "navPillPadding",
    )

    Row(
        modifier = modifier
            .height(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(20.dp)) {
            Icon(imageVector = tab.icon, contentDescription = tab.label, tint = contentColor)
        }
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            androidx.compose.material3.Text(
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
