package com.shameem.finora.core.ui

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shameem.finora.core.designsystem.FinoraColors

/** The app's toggle: teal track when on (iOS `Toggle().tint(brandSecondary)`). */
@Composable
fun FinoraSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = FinoraColors.BrandSecondary,
            checkedThumbColor = FinoraColors.BrandPrimary,
            uncheckedTrackColor = FinoraColors.FillSubtle,
            uncheckedThumbColor = FinoraColors.TextTertiary,
            uncheckedBorderColor = FinoraColors.Hairline,
        ),
    )
}
