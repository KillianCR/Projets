package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A tiny, label-free trend line for compact spaces (ticker cards) — same marks, no chrome. */
@Composable
fun Sparkline(values: List<Double>, lineColor: Color, modifier: Modifier = Modifier, height: Dp = 32.dp) {
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: (max.takeIf { it > 0 } ?: 1.0) * 0.05

        val stepX = size.width / (values.size - 1)
        fun yFor(v: Double) = size.height - ((v - min) / range).toFloat() * size.height

        val offsets = values.mapIndexed { i, v -> Offset(i * stepX, yFor(v)) }
        val linePath = Path().apply {
            moveTo(offsets[0].x, offsets[0].y)
            for (i in 0 until offsets.size - 1) {
                val p0 = offsets[i]
                val p1 = offsets[i + 1]
                val midX = (p0.x + p1.x) / 2f
                cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }
        }
        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(offsets.last().x, size.height)
            lineTo(offsets.first().x, size.height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0f))),
        )
        drawPath(path = linePath, color = lineColor, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}
