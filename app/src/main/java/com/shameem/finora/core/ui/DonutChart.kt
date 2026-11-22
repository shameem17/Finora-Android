package com.shameem.finora.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** One segment of a [DonutChart]; [id] keys its animation so it morphs rather than jumps. */
data class DonutSlice(
    val id: String,
    val value: Double,
    val color: Color,
)

/**
 * Donut chart equivalent of the iOS `Chart { SectorMark(...) }` (inner radius ratio 0.74, 3pt angular
 * inset, 6pt corner radius). Segment values animate when the data changes; [content] is centred in the ring.
 */
@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    innerRadiusRatio: Float = 0.74f,
    gap: Dp = 3.dp,
    cornerRadius: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animatedValues: List<Float> = slices.map { slice ->
        key(slice.id) {
            animateFloatAsState(
                targetValue = slice.value.toFloat(),
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "donut-${slice.id}",
            ).value
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawDonut(slices, animatedValues, innerRadiusRatio, gap.toPx(), cornerRadius.toPx())
        }
        content()
    }
}

private fun DrawScope.drawDonut(
    slices: List<DonutSlice>,
    values: List<Float>,
    innerRadiusRatio: Float,
    gapPx: Float,
    cornerPx: Float,
) {
    val outer = min(size.width, size.height) / 2f
    val inner = outer * innerRadiusRatio
    val thickness = outer - inner
    val mid = (outer + inner) / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    val total = values.sum()
    if (total <= 0f) return

    val visibleCount = values.count { it > 0f }
    val gapDegrees = if (visibleCount > 1) Math.toDegrees((gapPx / mid).toDouble()).toFloat() else 0f

    var start = -90f // 12 o'clock, clockwise
    slices.forEachIndexed { index, slice ->
        val fullSweep = values[index] / total * 360f
        if (fullSweep > 0f) {
            if (visibleCount == 1) {
                drawCircle(color = slice.color, radius = mid, center = center, style = Stroke(width = thickness))
            } else {
                val sweep = (fullSweep - gapDegrees).coerceAtLeast(0.5f)
                drawPath(
                    path = roundedRingSegment(center, inner, outer, start + gapDegrees / 2f, sweep, cornerPx),
                    color = slice.color,
                )
            }
        }
        start += fullSweep
    }
}

/**
 * Annular sector with all four corners rounded by fillets of radius `corner`.
 *
 * A fillet is tangent to a radial edge and to the outer circle (fillet centre at radius `ro - c`) or the
 * inner circle (centre at `ri + c`). The centre sits at angular offset `asin(c / (ro - c))` (outer) or
 * `asin(c / (ri + c))` (inner) from the edge, and the fillet sweeps `90 + alphaOuter` / `90 - alphaInner`
 * degrees. (Verified numerically: every arc end lands exactly on the next segment of the outline.)
 */
private fun roundedRingSegment(
    center: Offset,
    innerR: Float,
    outerR: Float,
    startDeg: Float,
    sweepDeg: Float,
    corner: Float,
): Path {
    val ro = outerR.toDouble()
    val ri = innerR.toDouble()
    val a0 = startDeg.toDouble()
    val a1 = a0 + sweepDeg

    fun point(angleDeg: Double, radius: Double) = Offset(
        center.x + (radius * cos(Math.toRadians(angleDeg))).toFloat(),
        center.y + (radius * sin(Math.toRadians(angleDeg))).toFloat(),
    )

    // Largest corner radius that still fits this segment's angular width and the band thickness.
    val sinHalf = sin(Math.toRadians(min(sweepDeg.toDouble(), 180.0) / 2.0))
    val maxByInner = if (sinHalf >= 0.999) Double.MAX_VALUE else ri * sinHalf / (1.0 - sinHalf)
    val maxByOuter = ro * sinHalf / (1.0 + sinHalf)
    val c = minOf(corner.toDouble(), (ro - ri) / 2.0, maxByInner, maxByOuter)

    val path = Path()
    if (c < 0.5) {
        path.arcTo(Rect(center, outerR), startDeg, sweepDeg, true)
        val innerEnd = point(a1, ri)
        path.lineTo(innerEnd.x, innerEnd.y)
        path.arcTo(Rect(center, innerR), a1.toFloat(), -sweepDeg, false)
        path.close()
        return path
    }

    val alphaOuter = Math.toDegrees(asin(c / (ro - c)))
    val alphaInner = Math.toDegrees(asin(c / (ri + c)))

    fun fillet(centerPoint: Offset, startAngle: Double, sweep: Double) {
        path.arcTo(Rect(centerPoint, c.toFloat()), startAngle.toFloat(), sweep.toFloat(), false)
    }

    // Outer-start corner
    val outerStartEdge = point(a0, (ro - c) * cos(Math.toRadians(alphaOuter)))
    path.moveTo(outerStartEdge.x, outerStartEdge.y)
    fillet(point(a0 + alphaOuter, ro - c), a0 - 90.0, 90.0 + alphaOuter)

    // Outer arc (clockwise) and outer-end corner
    path.arcTo(Rect(center, outerR), (a0 + alphaOuter).toFloat(), (sweepDeg - 2 * alphaOuter).toFloat(), false)
    fillet(point(a1 - alphaOuter, ro - c), a1 - alphaOuter, 90.0 + alphaOuter)

    // Down the end edge, inner-end corner
    val innerEndEdge = point(a1, (ri + c) * cos(Math.toRadians(alphaInner)))
    path.lineTo(innerEndEdge.x, innerEndEdge.y)
    fillet(point(a1 - alphaInner, ri + c), a1 + 90.0, 90.0 - alphaInner)

    // Inner arc (counter-clockwise), inner-start corner, close up the start edge
    path.arcTo(Rect(center, innerR), (a1 - alphaInner).toFloat(), (-(sweepDeg - 2 * alphaInner)).toFloat(), false)
    fillet(point(a0 + alphaInner, ri + c), a0 + alphaInner + 180.0, 90.0 - alphaInner)
    path.close()
    return path
}
