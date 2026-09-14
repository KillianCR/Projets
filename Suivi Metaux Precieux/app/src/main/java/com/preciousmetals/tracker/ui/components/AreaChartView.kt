package com.preciousmetals.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.preciousmetals.tracker.util.formatFr
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * [referenceValue], when set, is what the tooltip's percent/gain are measured against for THIS
 * point specifically — e.g. the portfolio's own cost basis on that exact day, so a deposit doesn't
 * read as a price gain. Left null (the default), the chart falls back to comparing every point
 * against [points]' first entry instead — "performance since the start of the displayed range",
 * which is what a plain spot-price series wants.
 */
data class ChartPoint(val date: LocalDate, val value: Double, val referenceValue: Double? = null)

/**
 * A real interactive price chart: smooth line, gradient area fill, recessive gridlines, a
 * few axis labels, and a drag/tap crosshair + tooltip (the dataviz skill's "hover layer, by
 * default" — the only form that skips it is a bare stat tile).
 *
 * The tooltip shows the selected point's percent change and, when [showGainAmount] is set (used by
 * the portfolio value history, not the per-metal spot price chart), its absolute gain/loss too —
 * both measured against that point's own [ChartPoint.referenceValue] when it has one, or against
 * [points]' first entry otherwise.
 */
@Composable
fun AreaChartView(
    points: List<ChartPoint>,
    lineColor: Color,
    valueFormatter: (Double) -> String,
    modifier: Modifier = Modifier,
    showGainAmount: Boolean = false,
) {
    if (points.size < 2) {
        Box(
            modifier = modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Pas encore assez de données",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val gridColor = MaterialTheme.colorScheme.outline
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val density = LocalDensity.current

    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }

    Box(modifier = modifier.fillMaxWidth().height(220.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures(onPress = { offset ->
                        selectedIndex = nearestIndex(offset.x, size.width, points.size)
                    })
                }
                .pointerInput(points) {
                    detectDragGestures(
                        onDragStart = { offset -> selectedIndex = nearestIndex(offset.x, size.width, points.size) },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null },
                    ) { change, _ ->
                        selectedIndex = nearestIndex(change.position.x, size.width, points.size)
                    }
                }
        ) {
            val leftPad = 8.dp.toPx()
            val rightPad = 8.dp.toPx()
            val topPad = 12.dp.toPx()
            val bottomPad = 28.dp.toPx() // room for date labels
            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad

            val values = points.map { it.value }
            val minValue = values.min()
            val maxValue = values.max()
            val range = (maxValue - minValue).takeIf { it > 0.0 } ?: (maxValue.takeIf { it > 0 } ?: 1.0) * 0.02

            fun xFor(index: Int): Float =
                leftPad + if (points.size == 1) 0f else (index.toFloat() / (points.size - 1)) * chartWidth
            fun yFor(value: Double): Float =
                topPad + chartHeight - ((value - (minValue - range * 0.08)) / (range * 1.16)).toFloat() * chartHeight

            // Recessive gridlines.
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPad + chartHeight * (i / gridLines.toFloat())
                drawLine(
                    color = gridColor.copy(alpha = 0.35f),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Smooth line through the points (midpoint cubic technique).
            val linePath = Path()
            val offsets = points.indices.map { i -> Offset(xFor(i), yFor(points[i].value)) }
            linePath.moveTo(offsets[0].x, offsets[0].y)
            for (i in 0 until offsets.size - 1) {
                val p0 = offsets[i]
                val p1 = offsets[i + 1]
                val midX = (p0.x + p1.x) / 2f
                linePath.cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
            }

            // Gradient area fill under the line.
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(offsets.last().x, topPad + chartHeight)
                lineTo(offsets.first().x, topPad + chartHeight)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.32f), lineColor.copy(alpha = 0f)),
                    startY = topPad,
                    endY = topPad + chartHeight,
                ),
            )

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )

            // Axis labels: min / max price, and first / mid / last date.
            val labelPaint = android.graphics.Paint().apply {
                color = mutedColor.toArgb()
                textSize = with(density) { 11.sp.toPx() }
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.apply {
                drawText(valueFormatter(maxValue), leftPad, topPad, labelPaint)
                drawText(valueFormatter(minValue), leftPad, topPad + chartHeight, labelPaint)

                val dateY = size.height - 8.dp.toPx()
                labelPaint.textAlign = android.graphics.Paint.Align.LEFT
                drawText(points.first().date.formatFr(), xFor(0), dateY, labelPaint)
                labelPaint.textAlign = android.graphics.Paint.Align.CENTER
                drawText(points[points.size / 2].date.formatFr(), xFor(points.size / 2), dateY, labelPaint)
                labelPaint.textAlign = android.graphics.Paint.Align.RIGHT
                drawText(points.last().date.formatFr(), xFor(points.size - 1), dateY, labelPaint)
            }

            // Crosshair + selected point.
            selectedIndex?.let { index ->
                val point = offsets[index]
                drawLine(
                    color = gridColor,
                    start = Offset(point.x, topPad),
                    end = Offset(point.x, topPad + chartHeight),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                )
                drawCircle(color = surfaceColor, radius = 7.dp.toPx(), center = point)
                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = point)
            }
        }

        selectedIndex?.let { index ->
            val point = points[index]
            val referenceValue = point.referenceValue ?: points.first().value
            val percentChange = if (referenceValue != 0.0) {
                ((point.value - referenceValue) / referenceValue) * 100.0
            } else {
                null
            }
            val gainAmountText = if (showGainAmount) {
                val gain = point.value - referenceValue
                (if (gain >= 0) "+" else "") + valueFormatter(gain)
            } else {
                null
            }
            ChartTooltip(
                point = point,
                text = valueFormatter(point.value),
                percentChange = percentChange,
                gainAmountText = gainAmountText,
                pointCount = points.size,
                index = index,
                lineColor = lineColor,
                onSurfaceColor = onSurfaceColor,
                surfaceColor = surfaceColor,
            )
        }
    }
}

@Composable
private fun BoxScope.ChartTooltip(
    point: ChartPoint,
    text: String,
    percentChange: Double?,
    gainAmountText: String?,
    pointCount: Int,
    index: Int,
    lineColor: Color,
    onSurfaceColor: Color,
    surfaceColor: Color,
) {
    val fraction = if (pointCount <= 1) 0f else index.toFloat() / (pointCount - 1)
    val alignment = when {
        fraction < 0.25f -> Alignment.TopStart
        fraction > 0.75f -> Alignment.TopEnd
        else -> Alignment.TopCenter
    }
    Surface(
        modifier = Modifier.align(alignment).padding(top = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = surfaceColor,
        shadowElevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text, color = onSurfaceColor, style = MaterialTheme.typography.labelLarge)
                if (percentChange != null) {
                    PercentPill(percent = percentChange)
                }
            }
            if (gainAmountText != null) {
                Text(
                    gainAmountText,
                    color = onSurfaceColor,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Text(
                point.date.formatFr(),
                color = lineColor,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private fun nearestIndex(x: Float, width: Int, count: Int): Int {
    if (count <= 1) return 0
    val fraction = (x / width).coerceIn(0f, 1f)
    return (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)
}
