package com.shameem.finora.core.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.appTextStyle

/** Manrope text with the size/weight call shape used across the iOS code base. */
@Composable
fun AppText(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    weight: AppFontWeight = AppFontWeight.Regular,
    color: Color = LocalContentColor.current,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        softWrap = softWrap,
        overflow = overflow,
        style = appTextStyle(size = size, weight = weight, letterSpacing = letterSpacing),
    )
}
