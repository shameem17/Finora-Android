package com.shameem.finora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.FinoraColors

/** Cream card with a hairline border and soft shadow (iOS `.finoraCard()`). */
fun Modifier.finoraCard(cornerRadius: Dp = 28.dp): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = 14.dp,
            shape = shape,
            ambientColor = FinoraColors.BrandSecondary.copy(alpha = 0.10f),
            spotColor = FinoraColors.BrandSecondary.copy(alpha = 0.10f),
        )
        .background(FinoraColors.CardSurface, shape)
        .border(1.dp, FinoraColors.Hairline, shape)
}
