package com.shameem.finora.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors

/** Large navigation-style title shown at the top of a tab (iOS `prefersLargeTitles` navigation title). */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    AppText(
        text = text,
        size = 34,
        weight = AppFontWeight.Bold,
        color = FinoraColors.TextPrimary,
        maxLines = 1,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 4.dp),
    )
}
