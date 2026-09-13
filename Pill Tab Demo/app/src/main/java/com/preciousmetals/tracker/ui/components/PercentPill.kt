package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.ui.theme.PillSurfaceDark
import com.preciousmetals.tracker.util.formatPercent

/**
 * A rounded +/- percent badge, shared by every gain/loss display (plus-value, market stats).
 * Per the mockup, this is a plain white-on-translucent pill — no trend icon, no red/green tint —
 * consistent with the redesign's single unified white accent rather than a mixed status palette.
 */
@Composable
fun PercentPill(percent: Double, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = PillSurfaceDark,
        modifier = modifier,
    ) {
        Text(
            formatPercent(percent),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
    }
}
