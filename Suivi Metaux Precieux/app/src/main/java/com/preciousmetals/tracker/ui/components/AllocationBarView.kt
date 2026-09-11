package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AllocationSlice(val label: String, val value: Double, val color: Color)

/**
 * A 100%-stacked bar with a legend — the exact-value, low-footprint alternative to a donut chart
 * for a handful of categories (see the ui-ux-pro-max chart guidance: donuts read proportion at a
 * glance but hide precise shares and, at a fixed 1:1 aspect ratio, cost a full screen-width of
 * height; a stacked bar states the split with roughly a sixth the vertical space).
 */
@Composable
fun AllocationBarView(slices: List<AllocationSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
        ) {
            if (total > 0.0) {
                slices.forEach { slice ->
                    val fraction = (slice.value / total).toFloat().coerceIn(0f, 1f)
                    if (fraction > 0f) {
                        Box(modifier = Modifier.weight(fraction).fillMaxHeight().background(slice.color))
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(top = 12.dp)) {
            slices.forEach { slice ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.size(10.dp).background(slice.color, CircleShape)
                    )
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 10.dp).weight(1f),
                    )
                    val percent = if (total > 0.0) (slice.value / total * 100.0) else 0.0
                    Text(
                        text = String.format("%.1f %%", percent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
