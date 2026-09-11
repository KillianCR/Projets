package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.domain.model.Metal

/** A small coin-style "logo" for a metal: a tinted circle with a monetary glyph, no external asset needed. */
@Composable
fun MetalLogo(metal: Metal, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val color = Color(metal.colorHex)
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.MonetizationOn,
            contentDescription = metal.displayNameFr,
            tint = color,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}
