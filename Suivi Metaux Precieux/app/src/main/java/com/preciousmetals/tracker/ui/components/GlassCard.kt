package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.ui.theme.CardBorderDark
import com.preciousmetals.tracker.ui.theme.CardSurfaceDark

/**
 * The one card look shared by every surface in the redesign (plus-value, metals, allocation,
 * calculator, ...): a translucent white fill over the ember background instead of a flat opaque
 * fill, with a matching hairline border — replacing the previously disparate per-screen card
 * styles (report: "Cartes uniformisées"). Fill/border values are the mockup's exact
 * #ffffff0a / #ffffff1a, not the theme's generic (and more opaque) surfaceVariant/outline.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: CardColors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val border = BorderStroke(1.dp, CardBorderDark)
    val inner: @Composable () -> Unit = { Column(modifier = Modifier.padding(contentPadding)) { content() } }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) { inner() }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) { inner() }
    }
}
