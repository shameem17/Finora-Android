package com.shameem.finora.core.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints

/**
 * Single-line text that scales its font down (never below [minScale]) instead of wrapping or
 * clipping; the Compose counterpart of SwiftUI's `lineLimit(1).minimumScaleFactor(_)`.
 *
 * The scale is derived from a text measurement on every composition rather than by feedback
 * through layout callbacks, so it never flickers while the text is animating.
 */
@Composable
fun ShrinkToFitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    alignment: Alignment = Alignment.CenterStart,
    textAlign: TextAlign = TextAlign.Unspecified,
) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier, contentAlignment = alignment) {
        val maxWidthPx = constraints.maxWidth
        val scale = if (maxWidthPx == Constraints.Infinity) {
            1f
        } else {
            val measured = measurer.measure(text = text, style = style, maxLines = 1, softWrap = false).size.width
            if (measured > maxWidthPx && measured > 0) {
                (maxWidthPx.toFloat() / measured).coerceAtLeast(minScale)
            } else {
                1f
            }
        }
        androidx.compose.material3.Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            textAlign = textAlign,
            style = style.copy(fontSize = style.fontSize * scale),
        )
    }
}

/** Convenience wrapper that fills the available width. */
@Composable
fun ShrinkToFitTextFillWidth(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    alignment: Alignment = Alignment.Center,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = alignment) {
        ShrinkToFitText(text = text, style = style, color = color, minScale = minScale, alignment = alignment)
    }
}
