package com.shameem.finora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip

/** Thin capsule progress bar; the fill is never narrower than 6dp (iOS `ProgressBar`). */
@Composable
fun ProgressBar(
    fraction: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
    ) {
        val fill = (maxWidth * fraction.toFloat().coerceIn(0f, 1f)).coerceAtLeast(6.dp)
        Box(
            Modifier
                .width(fill)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color),
        )
    }
}
