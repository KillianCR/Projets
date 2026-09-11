package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.preciousmetals.tracker.ui.theme.negativeColor
import com.preciousmetals.tracker.ui.theme.positiveColor
import com.preciousmetals.tracker.util.formatPercent

/** A rounded, tinted +/- percent badge with a trend arrow, shared by every gain/loss display. */
@Composable
fun PercentPill(percent: Double, modifier: Modifier = Modifier) {
    val statColor = if (percent >= 0) positiveColor() else negativeColor()
    Surface(
        shape = RoundedCornerShape(50),
        color = statColor.copy(alpha = 0.16f),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Icon(
                if (percent >= 0) Icons.AutoMirrored.Outlined.TrendingUp else Icons.AutoMirrored.Outlined.TrendingDown,
                contentDescription = if (percent >= 0) "En hausse" else "En baisse",
                tint = statColor,
                modifier = Modifier.padding(end = 4.dp),
            )
            Text(
                formatPercent(percent),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statColor,
            )
        }
    }
}
