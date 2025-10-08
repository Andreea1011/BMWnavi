package com.example.bmwnavi.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * history: list of (kmIndex in 0..1000, fuelPercent in 0..100)
 */
@Composable
fun FuelTabContent(
    history: List<Pair<Float, Float>>,
    remainingKm: Int,
    avgTripLPer100: Double,
    kmSinceFull: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            FuelChart(
                history = history,
                modifier = Modifier.fillMaxSize().padding(12.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Centered lines under the chart
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Remaining km: $remainingKm",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Average consumption: ${String.format("%.1f L/100km", avgTripLPer100)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Km driven since last full: ${kmSinceFull.roundToInt()}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun FuelChart(
    history: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    val axis = MaterialTheme.colorScheme.outline
    val line = MaterialTheme.colorScheme.primary
    val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val grid = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val labels = MaterialTheme.colorScheme.onBackground

    Canvas(modifier) {
        val padding = 48f
        val left = padding
        val right = size.width - padding
        val top = padding
        val bottom = size.height - padding

        // Axes
        drawLine(axis, start = Offset(left, top), end = Offset(left, bottom), strokeWidth = 2f)
        drawLine(axis, start = Offset(left, bottom), end = Offset(right, bottom), strokeWidth = 2f)

        // Grid Y (0,25,50,75,100)
        val ySteps = 4
        for (i in 0..ySteps) {
            val t = i / ySteps.toFloat() // 0..1
            val y = bottom - (bottom - top) * t
            drawLine(grid, Offset(left, y), Offset(right, y), strokeWidth = 1f)
            drawChartLabel("${(t * 100).roundToInt()}%", left - 10f, y - 4f, alignRight = true, color = labels)
        }

        // Grid X (0..1000 every 200)
        val xMax = 1000f
        val xGrid = 200
        for (xv in 0..xMax.toInt() step xGrid) {
            val x = left + (right - left) * (xv / xMax)
            drawLine(grid, Offset(x, top), Offset(x, bottom), strokeWidth = 1f)
            drawChartLabel(xv.toString(), x, bottom + 24f, center = true, color = labels)
        }

        // Build path from history mapped to [0..1000] km and 0..100%
        if (history.isNotEmpty()) {
            val firstKm = history.first().first
            val lastKm = history.last().first
            val span = (lastKm - firstKm).coerceAtLeast(1f)
            val path = Path()
            var started = false

            fun mapX(km: Float): Float {
                val kmNorm = if (span < 1000f) (km - firstKm) else (km - (lastKm - 1000f))
                val clamped = kmNorm.coerceIn(0f, 1000f)
                return left + (right - left) * (clamped / 1000f)
            }
            fun mapY(pct: Float): Float {
                val t = (pct.coerceIn(0f, 100f) / 100f)
                return bottom - (bottom - top) * t
            }

            // area fill
            val areaPath = Path()
            for (pt in history) {
                val x = mapX(pt.first)
                val y = mapY(pt.second)
                if (!started) {
                    started = true
                    path.moveTo(x, y)
                    areaPath.moveTo(x, bottom)
                    areaPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    areaPath.lineTo(x, y)
                }
            }
            if (started) {
                val lastX = mapX(history.last().first)
                areaPath.lineTo(lastX, bottom)
                areaPath.close()
                drawPath(areaPath, color = fill)
            }

            // main line + soft glow
            drawPath(
                path = path,
                brush = Brush.linearGradient(listOf(line.copy(alpha = 0.95f), line)),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
            drawPath(
                path = path,
                color = line.copy(alpha = 0.15f),
                style = Stroke(width = 13f, cap = StrokeCap.Round)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChartLabel(
    text: String,
    x: Float,
    y: Float,
    alignRight: Boolean = false,
    center: Boolean = false,
    color: Color
) {
    val nc = drawContext.canvas.nativeCanvas
    val p = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 28f
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        textAlign = when {
            center -> android.graphics.Paint.Align.CENTER
            alignRight -> android.graphics.Paint.Align.RIGHT
            else -> android.graphics.Paint.Align.LEFT
        }
    }
    nc.drawText(text, x, y, p)
}
