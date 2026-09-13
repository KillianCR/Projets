package com.preciousmetals.tracker.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Metal
import com.preciousmetals.tracker.ui.theme.CardBorderDark
import com.preciousmetals.tracker.ui.theme.NearBlackEmber

/**
 * The app's [com.preciousmetals.tracker.ui.components.GlassCard] look (translucent fill + hairline
 * border on the ember background), reproduced for Glance widgets. Glance has no border modifier,
 * so the border is faked with two nested rounded boxes — an outer one the border color, an inner
 * one inset by the border width and filled with the card color.
 */
@Composable
fun WidgetCard(onClick: Action? = null, content: @Composable () -> Unit) {
    val radius = 20.dp
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(CardBorderDark))
            .cornerRadius(radius),
    ) {
        val inner = GlanceModifier
            .fillMaxSize()
            .padding(1.dp)
            .background(ColorProvider(NearBlackEmber))
            .cornerRadius(radius)
        Box(modifier = if (onClick != null) inner.clickable(onClick) else inner) {
            content()
        }
    }
}

/** Same "kilo for copper, gram for everything else" abbreviation used across the app's own UI. */
fun Metal.shortSmallUnitLabel(): String = if (smallUnitLabel == "kilo") "kg" else "g"
