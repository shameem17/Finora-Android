package com.shameem.finora.screens.onboarding.view

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * A filled region below a sine wave (iOS `WaveShape`).
 *
 * @param amplitude height of the crests
 * @param frequency number of full waves across the width
 * @param phase horizontal shift in radians
 * @param baseline vertical position of the wave's centre line, 0 = top, 1 = bottom
 */
class WaveShape(
    private val amplitude: Dp = 20.dp,
    private val frequency: Float = 1.5f,
    private val phase: Float = 0f,
    private val baseline: Float = 0.5f,
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val amplitudePx = with(density) { amplitude.toPx() }
        val midY = size.height * baseline
        val step = 2f // 2px sampling is visually identical to 1px and half the path size

        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, midY)
            var x = 0f
            while (x < size.width) {
                lineTo(x, waveY(x, size.width, midY, amplitudePx))
                x += step
            }
            lineTo(size.width, waveY(size.width, size.width, midY, amplitudePx))
            lineTo(size.width, size.height)
            close()
        }
        return Outline.Generic(path)
    }

    private fun waveY(x: Float, width: Float, midY: Float, amplitudePx: Float): Float {
        val angle = (x / width) * frequency * 2f * PI.toFloat() + phase
        return midY + sin(angle) * amplitudePx
    }
}
