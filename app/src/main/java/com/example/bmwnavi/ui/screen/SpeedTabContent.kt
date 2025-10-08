@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.bmwnavi.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

private const val MAX_SPEED = 260.0
private const val MAX_RPM = 7000.0
private const val SPEED_THRESHOLD = 130.0
private const val RPM_THRESHOLD = 3000.0

// stroke tokens
private const val FRAME_WIDTH = 4f
private const val PROGRESS_WIDTH = 10f
private const val MINI_PROGRESS_WIDTH = 7f

// --- Debug helpers ---
private const val SHOW_DEBUG_TEXT = true              // set false to hide debug numbers
private val FORCE_TEST_SPEED_LIMIT: Int? = null // e.g. set to 50 to force-show pill

@Composable
fun SpeedTabContent(
    speedKmh: Double,
    rpm: Double,
    fuelPercent: Double,
    coolantC: Double,
    remainingKm: Int?,
    dateStr: String,
    speedLimitKph: Int? = null,      // can be null (hidden)
    modifier: Modifier = Modifier
) {
    val vignette = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            // subtle vignette bg
            Canvas(Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(vignette.copy(alpha = 0.20f), Color.Transparent),
                        center = center,
                        radius = size.minDimension * 0.75f
                    )
                )
            }

            DualHexGauge(
                speedKmh = speedKmh.coerceIn(0.0, MAX_SPEED),
                rpm = rpm.coerceIn(0.0, MAX_RPM),
                fuelPercent = fuelPercent.coerceIn(0.0, 100.0),
                coolantC = coolantC.coerceIn(0.0, 120.0),
                modifier = Modifier.fillMaxSize()
            )

            // === Centered "Speed limit" pill overlay ===
            val limitToShow = FORCE_TEST_SPEED_LIMIT ?: speedLimitKph
            if (limitToShow != null) {
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                ) {
                    val over = speedKmh > limitToShow
                    val color = if (over) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                    Text(
                        text = "Speed limit: ${limitToShow} km/h",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = color
                    )
                }
            }

            // --- bottom debug readout (helps verify values are changing) ---
            if (SHOW_DEBUG_TEXT) {
                Text(
                    text = "spd=${"%.0f".format(speedKmh)}  rpm=${"%.0f".format(rpm)}  fuel=${"%.1f".format(fuelPercent)}%  cool=${"%.1f".format(coolantC)}°C",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Remaining KM: ${remainingKm?.toString() ?: "—"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(dateStr, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DualHexGauge(
    speedKmh: Double,
    rpm: Double,
    fuelPercent: Double,
    coolantC: Double,
    modifier: Modifier = Modifier
) {
    val frame = MaterialTheme.colorScheme.outline
    val ok = MaterialTheme.colorScheme.primary
    val warn = MaterialTheme.colorScheme.error
    val fuelGreen = MaterialTheme.colorScheme.tertiary
    val fuelYellow = MaterialTheme.colorScheme.secondary
    val fuelRed = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onBackground

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Main gauges geometry (top row)
        val leftTop = Offset(w * 0.08f, h * 0.12f)
        val rightTop = Offset(w * 0.52f, h * 0.12f)
        val gaugeW = w * 0.40f
        val gaugeH = h * 0.50f

        val leftPoly = halfHexPolyline(
            bottomToTop = true, topLeft = leftTop, w = gaugeW, h = gaugeH, leftSide = true
        )
        val rightPoly = halfHexPolyline(
            bottomToTop = true, topLeft = rightTop, w = gaugeW, h = gaugeH, leftSide = false
        )

        // Frames
        drawPolylinePretty(leftPoly, frame, FRAME_WIDTH, glow = false)
        drawPolylinePretty(rightPoly, frame, FRAME_WIDTH, glow = false)

        // Ticks (major + small minors)
        drawTicksFancy(leftPoly, major = 6, minorPerSegment = 1, color = frame)
        drawTicksFancy(rightPoly, major = 6, minorPerSegment = 1, color = frame)

        // Progress
        val speedT = (speedKmh / MAX_SPEED).toFloat().coerceIn(0f, 1f)
        val rpmT = (rpm / MAX_RPM).toFloat().coerceIn(0f, 1f)

        drawProgressPretty(
            leftPoly, speedT,
            if (speedKmh <= SPEED_THRESHOLD) ok else warn, PROGRESS_WIDTH
        )
        drawProgressPretty(
            rightPoly, rpmT,
            if (rpm <= RPM_THRESHOLD) ok else warn, PROGRESS_WIDTH
        )

        // ------- Numeric labels on main gauges -------
        // Speed: 0, 130, 260
        drawLabelAt(leftPoly, t = 0f, text = "0", color = labelColor, dy = 26f)
        drawLabelAt(leftPoly, t = (SPEED_THRESHOLD / MAX_SPEED).toFloat(), text = "130", color = labelColor, dy = -8f)
        drawLabelAt(leftPoly, t = 1f, text = "260", color = labelColor, dy = -10f)

        // RPM: 0, 3000, 7000
        drawLabelAt(rightPoly, t = 0f, text = "0", color = labelColor, dy = 26f, alignRight = true)
        drawLabelAt(rightPoly, t = (RPM_THRESHOLD / MAX_RPM).toFloat(), text = "3000", color = labelColor, dy = -8f, alignRight = true)
        drawLabelAt(rightPoly, t = 1f, text = "7000", color = labelColor, dy = -10f, alignRight = true)

        // ------- Mini gauges (bottom row): Fuel % (left), Coolant °C (right) -------
        val miniW = w * 0.22f
        val miniH = h * 0.22f

        // Fuel % (left bottom)
        val fuelTopLeft = Offset(w * 0.10f, h * 0.72f)
        val fuelPoly = halfHexPolyline(bottomToTop = true, topLeft = fuelTopLeft, w = miniW, h = miniH, leftSide = true)
        drawPolylinePretty(fuelPoly, frame, FRAME_WIDTH, glow = false)
        val fuelT = (fuelPercent / 100.0).toFloat().coerceIn(0f, 1f)
        val fuelColor = when {
            fuelPercent >= 75.0 -> fuelGreen
            fuelPercent >= 25.0 -> fuelYellow
            else -> fuelRed
        }
        drawProgressPretty(fuelPoly, fuelT, fuelColor, MINI_PROGRESS_WIDTH)
        drawCaptionBelowBox(fuelTopLeft, miniW, miniH, "Fuel %", labelColor)

        // Coolant °C (right bottom)
        val coolTopLeft = Offset(w * 0.68f, h * 0.72f)
        val coolPoly = halfHexPolyline(bottomToTop = true, topLeft = coolTopLeft, w = miniW, h = miniH, leftSide = false)
        drawPolylinePretty(coolPoly, frame, FRAME_WIDTH, glow = false)
        val coolT = (coolantC.toFloat() / 120f).coerceIn(0f, 1f)
        drawProgressPretty(coolPoly, coolT, ok, MINI_PROGRESS_WIDTH)
        drawCaptionBelowBox(coolTopLeft, miniW, miniH, "Coolant °C", labelColor)
    }
}

/* ===================== drawing helpers ===================== */

private typealias Polyline = List<Offset>

/** half-hex polyline; if bottomToTop=true, index 0 is bottom → top at end */
private fun DrawScope.halfHexPolyline(
    bottomToTop: Boolean,
    topLeft: Offset,
    w: Float,
    h: Float,
    leftSide: Boolean
): Polyline {
    val x0 = topLeft.x
    val y0 = topLeft.y
    val x1 = x0 + w
    val y1 = y0 + h
    val inset = w * 0.18f
    val midY = (y0 + y1) / 2f

    val pts = if (leftSide) {
        listOf(
            Offset(x0 + inset, y0), // top
            Offset(x0, midY),       // mid
            Offset(x0 + inset, y1)  // bottom
        )
    } else {
        listOf(
            Offset(x1 - inset, y0),
            Offset(x1, midY),
            Offset(x1 - inset, y1)
        )
    }
    return if (bottomToTop) pts.reversed() else pts
}

private fun DrawScope.drawPolylinePretty(
    poly: Polyline,
    baseColor: Color,
    width: Float,
    glow: Boolean = true
) {
    if (glow) {
        // soft outer glows
        for (i in 0 until poly.lastIndex) {
            drawLine(
                color = baseColor.copy(alpha = 0.20f),
                start = poly[i],
                end = poly[i + 1],
                strokeWidth = width * 2.2f,
                cap = StrokeCap.Round
            )
        }
        for (i in 0 until poly.lastIndex) {
            drawLine(
                color = baseColor.copy(alpha = 0.08f),
                start = poly[i],
                end = poly[i + 1],
                strokeWidth = width * 4.0f,
                cap = StrokeCap.Round
            )
        }
    }
    // gradient stroke (slightly brighter towards the head)
    for (i in 0 until poly.lastIndex) {
        val a = poly[i]; val b = poly[i + 1]
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(baseColor.copy(alpha = 0.95f), baseColor),
                start = a, end = b
            ),
            start = a, end = b, strokeWidth = width, cap = StrokeCap.Round
        )
    }
}

private fun segmentLength(a: Offset, b: Offset): Float =
    hypot((b.x - a.x).toDouble(), (b.y - a.y).toDouble()).toFloat()

private fun polylineTotalLength(poly: Polyline): Float {
    var total = 0f
    for (i in 0 until poly.lastIndex) total += segmentLength(poly[i], poly[i + 1])
    return total
}

/** Draw the polyline up to fraction t (0..1), using pretty gradient + glow. */
private fun DrawScope.drawProgressPretty(poly: Polyline, t: Float, color: Color, width: Float) {
    val clamped = t.coerceIn(0f, 1f)
    val totalLen = polylineTotalLength(poly)
    var target = totalLen * clamped
    if (target <= 0f) return

    val partial = mutableListOf(poly.first())
    loop@ for (i in 0 until poly.lastIndex) {
        val a = poly[i]; val b = poly[i + 1]
        val segLen = segmentLength(a, b)
        if (target >= segLen) {
            partial += b
            target -= segLen
        } else {
            val r = if (segLen == 0f) 0f else target / segLen
            val x = a.x + (b.x - a.x) * r
            val y = a.y + (b.y - a.y) * r
            partial += Offset(x, y)
            break@loop
        }
    }
    drawPolylinePretty(partial, color, width, glow = true)
}

/** Major ticks + optional small minors per segment */
private fun DrawScope.drawTicksFancy(
    poly: Polyline,
    major: Int,
    minorPerSegment: Int = 0,
    color: Color
) {
    // majors spaced along whole polyline
    val totalLen = polylineTotalLength(poly)
    val step = totalLen / (major + 1)
    var nextAt = step
    var acc = 0f
    val majorLen = 12f

    for (i in 0 until poly.lastIndex) {
        val a = poly[i]; val b = poly[i + 1]
        val segLen = segmentLength(a, b)

        while (nextAt <= acc + segLen) {
            val along = (nextAt - acc) / segLen
            val px = a.x + (b.x - a.x) * along
            val py = a.y + (b.y - a.y) * along
            drawLine(
                color = color,
                start = Offset(px - majorLen, py),
                end = Offset(px + majorLen, py),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            nextAt += step
        }
        acc += segLen
    }

    // minors on each segment
    if (minorPerSegment > 0) {
        for (i in 0 until poly.lastIndex) {
            val a = poly[i]; val b = poly[i + 1]
            for (m in 1..minorPerSegment) {
                val r = m.toFloat() / (minorPerSegment + 1)
                val px = a.x + (b.x - a.x) * r
                val py = a.y + (b.y - a.y) * r
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = Offset(px - 6f, py),
                    end = Offset(px + 6f, py),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/** Interpolated point at fraction t along polyline (0..1). */
private fun polylinePointAt(poly: Polyline, t: Float): Offset {
    val clamped = t.coerceIn(0f, 1f)
    val total = polylineTotalLength(poly)
    var target = total * clamped
    for (i in 0 until poly.lastIndex) {
        val a = poly[i]; val b = poly[i + 1]
        val seg = segmentLength(a, b)
        if (target > seg) { target -= seg; continue }
        val r = if (seg == 0f) 0f else target / seg
        return Offset(a.x + (b.x - a.x) * r, a.y + (b.y - a.y) * r)
    }
    return poly.last()
}

/** Small label near fraction t of the polyline (with tiny glow). */
private fun DrawScope.drawLabelAt(
    poly: Polyline,
    t: Float,
    text: String,
    color: Color,
    dy: Float = 0f,
    alignRight: Boolean = false,
    size: Float = 34f
) {
    val pt = polylinePointAt(poly, t)
    val nc = drawContext.canvas.nativeCanvas
    val p = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = size
        setShadowLayer(6f, 0f, 0f, android.graphics.Color.argb(60, 0, 0, 0))
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        textAlign = if (alignRight) android.graphics.Paint.Align.RIGHT
        else android.graphics.Paint.Align.LEFT
    }
    nc.drawText(text, pt.x + if (alignRight) -10f else 10f, pt.y + dy, p)
}

/** Centered caption under a mini gauge box. */
private fun DrawScope.drawCaptionBelowBox(
    topLeft: Offset,
    w: Float,
    h: Float,
    text: String,
    color: Color
) {
    val x = topLeft.x + w / 2f
    val y = topLeft.y + h + 22f
    val nc = drawContext.canvas.nativeCanvas
    val p = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = 28f
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.argb(50, 0, 0, 0))
        this.color = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        textAlign = android.graphics.Paint.Align.CENTER
    }
    nc.drawText(text, x, y, p)
}
