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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.ui.theme.BlackEmber

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
 * its icon alone with no label.
 */
@Composable
fun FloatingBottomNav(
    tabs: List<BottomTab>,
    isSelected: (BottomTab) -> Boolean,
    onSelect: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = PillOuterMargin)
            .padding(bottom = PillOuterMargin)
            .clip(CircleShape)
            .background(BlackEmber)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            NavPill(tab = tab, selected = isSelected(tab), onClick = { onSelect(tab) })
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

@Composable
private fun NavPill(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "navPillBackground",
    )
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
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(background)
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
