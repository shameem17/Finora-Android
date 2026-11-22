package com.shameem.finora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.designsystem.toHexString

val DefaultColorPresets: List<String> = listOf(
    "#007366", "#2E7D32", "#1A4DB3", "#6640D9", "#A259FF", "#E31266",
    "#F2661A", "#B45309", "#1F2433", "#455A64", "#8E24AA",
)

private const val Columns = 6

/**
 * Preset colour circles plus a final "custom" circle that opens a hue/saturation/brightness dialog
 * (iOS `ColorSwatchPicker`, which used the system `ColorPicker`).
 */
@Composable
fun ColorSwatchPicker(
    hex: String,
    onHexChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    presets: List<String> = DefaultColorPresets,
) {
    var showCustom by remember { mutableStateOf(false) }
    val cells: List<String?> = presets + listOf<String?>(null) // null = the custom picker cell

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cells.chunked(Columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { preset ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (preset == null) {
                            CustomSwatch(onClick = { showCustom = true })
                        } else {
                            Swatch(
                                preset = preset,
                                isSelected = preset.equals(hex, ignoreCase = true),
                                onClick = { onHexChange(preset) },
                            )
                        }
                    }
                }
                // Keep the last row's cells the same width as the full rows.
                repeat(Columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }

    if (showCustom) {
        CustomColorDialog(
            initialHex = hex,
            onDismiss = { showCustom = false },
            onConfirm = {
                onHexChange(it)
                showCustom = false
            },
        )
    }
}

@Composable
private fun Swatch(preset: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(colorFromHex(preset))
            .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
            .semantics {
                contentDescription = "Colour $preset"
                selected = isSelected
            }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CustomSwatch(onClick: () -> Unit) {
    val rainbow = Brush.sweepGradient(
        listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(rainbow)
            .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
            .semantics { contentDescription = "Custom colour" }
            .clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun CustomColorDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initial = remember(initialHex) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(colorFromHex(initialHex).toArgbInt(), it) }
    }
    var hue by remember { mutableFloatStateOf(initial[0]) }
    var saturation by remember { mutableFloatStateOf(initial[1]) }
    var brightness by remember { mutableFloatStateOf(initial[2]) }
    val color = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness)))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FinoraColors.ScreenBackground,
        title = { AppText("Custom colour", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                ChannelSlider("Hue", hue, 0f..360f) { hue = it }
                ChannelSlider("Saturation", saturation, 0f..1f) { saturation = it }
                ChannelSlider("Brightness", brightness, 0f..1f) { brightness = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(color.toHexString()) }) {
                AppText("Done", size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText("Cancel", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary)
            }
        },
    )
}

@Composable
private fun ChannelSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        SectionLabel(label)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = FinoraColors.Accent,
                activeTrackColor = FinoraColors.Accent,
                inactiveTrackColor = FinoraColors.FillSubtle,
            ),
        )
    }
}

private fun Color.toArgbInt(): Int = this.toArgb()
