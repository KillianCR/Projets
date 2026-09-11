package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** A minimal line chart (no library dependency) for a series of (x label, y value) points. */
@Composable
fun LineChartView(
    values: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(180.dp)) {
        if (values.size < 2) {
            Text(
                text = "Pas encore assez de données",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Center),
            )
            return@Box
        }
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0

        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val stepX = size.width / (values.size - 1)
            val points = values.mapIndexed { index, value ->
                val normalized = (value - min) / range
                Offset(
                    x = index * stepX,
                    y = size.height - (normalized * size.height).toFloat(),
                )
            }
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = points[i],
                    end = points[i + 1],
                    strokeWidth = 5f,
                )
            }
            points.forEach { point ->
                drawCircle(color = lineColor, radius = 5f, center = point, style = Stroke(width = 2f))
            }
        }
    }
}
