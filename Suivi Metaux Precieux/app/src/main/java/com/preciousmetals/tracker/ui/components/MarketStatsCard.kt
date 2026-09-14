package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preciousmetals.tracker.ui.history.MarketStat
import com.preciousmetals.tracker.ui.theme.IconTileSurfaceDark
import com.preciousmetals.tracker.ui.theme.TextMuted44Dark
import com.preciousmetals.tracker.ui.theme.TextMuted67Dark

/**
 * A "STATISTIQUES DU MARCHÉ"-style card of Performance 1S/1M/3M/1A/5A rows — shared between
 * [com.preciousmetals.tracker.ui.history.PriceHistoryScreen] (per-metal) and
 * [com.preciousmetals.tracker.ui.history.PortfolioHistoryScreen] (whole-portfolio), which only
 * differ in [title] and in how each [MarketStat] was computed.
 */
@Composable
fun MarketStatsCard(stats: List<MarketStat>, title: String = "STATISTIQUES DU MARCHÉ") {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, letterSpacing = 0.5.sp),
            fontWeight = FontWeight.SemiBold,
            color = TextMuted44Dark,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp),
        )
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                stats.forEachIndexed { index, stat ->
                    MarketStatRow(stat = stat)
                    if (index != stats.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketStatRow(stat: MarketStat) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = IconTileSurfaceDark,
            ) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = TextMuted67Dark,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                "Performance ${stat.label}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        val percent = stat.percentChange
        if (percent == null) {
            Text("—", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            PercentPill(percent = percent)
        }
    }
}
