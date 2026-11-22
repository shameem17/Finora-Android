package com.shameem.finora.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors

private val PillSpacing = 4.dp

/** Segmented capsule control with a sliding tangerine indicator (iOS `PillPicker`). */
@Composable
fun <T : PickerOption> PillPicker(
    options: List<T>,
    selection: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(FinoraColors.CardSurface)
            .border(BorderStroke(1.dp, FinoraColors.Hairline), CircleShape)
            .padding(4.dp),
    ) {
        val pillWidth = (maxWidth - PillSpacing * (options.size - 1)) / options.size
        val selectedIndex = options.indexOf(selection).coerceAtLeast(0)
        val indicatorOffset by animateDpAsState(
            targetValue = (pillWidth + PillSpacing) * selectedIndex,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
            label = "pill-indicator",
        )

        Box(Modifier.matchParentSize()) {
            Box(
                Modifier
                    .offset(x = indicatorOffset)
                    .width(pillWidth)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(FinoraColors.Accent),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PillSpacing),
        ) {
            options.forEach { option ->
                val isSelected = option == selection
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) FinoraColors.OnAccent else FinoraColors.TextSecondary,
                    label = "pill-text",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .selectable(
                            selected = isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelectionChange(option) },
                        )
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppText(
                        text = option.title,
                        size = 14,
                        weight = AppFontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
