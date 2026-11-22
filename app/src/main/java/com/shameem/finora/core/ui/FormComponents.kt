package com.shameem.finora.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle

/** Text style for form inputs (iOS `.formFieldStyle()`). */
val FormFieldTextStyle: TextStyle
    get() = appTextStyle(size = 15, weight = AppFontWeight.SemiBold).copy(color = FinoraColors.TextPrimary)

/** Icon well + title on the left, [content] on the right (iOS `FormFieldRow`). */
@Composable
fun FormFieldRow(
    icon: String,
    title: String,
    modifier: Modifier = Modifier,
    alignTop: Boolean = false,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(FinoraColors.FillSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = sfSymbolIcon(icon),
                contentDescription = null,
                tint = FinoraColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
        AppText(
            text = title,
            size = 14,
            weight = AppFontWeight.SemiBold,
            color = FinoraColors.TextSecondary,
            maxLines = 1,
        )
        Spacer(Modifier.weight(1f))
        content()
    }
}

/** A title / value line (iOS `ValueRow`). */
@Composable
fun ValueRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AppText(
            text = title,
            size = 14,
            weight = if (emphasized) AppFontWeight.Bold else AppFontWeight.Medium,
            color = FinoraColors.TextPrimary.copy(alpha = if (emphasized) 1f else 0.6f),
        )
        Spacer(Modifier.weight(1f))
        AppText(
            text = value,
            size = 14,
            weight = if (emphasized) AppFontWeight.ExtraBold else AppFontWeight.SemiBold,
            color = FinoraColors.TextPrimary,
            textAlign = TextAlign.End,
        )
    }
}

/** Capsule search field with a clear button (iOS `SearchField`). */
@Composable
fun SearchField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    prompt: String = "Search",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FinoraColors.CardSurface, CircleShape)
            .border(1.dp, FinoraColors.Hairline, CircleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = FinoraColors.TextTertiary)
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) {
                AppText(text = prompt, size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextTertiary)
            }
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                singleLine = true,
                textStyle = FormFieldTextStyle,
                cursorBrush = SolidColor(FinoraColors.TextPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (text.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear search",
                tint = FinoraColors.TextTertiary,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { onTextChange("") },
            )
        }
    }
}

/** Inline validation message (iOS `FormErrorBanner`); renders nothing when [message] is null. */
@Composable
fun FormErrorBanner(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = message != null, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(FinoraColors.Negative.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = sfSymbolIcon("exclamationmark.triangle.fill"),
                contentDescription = null,
                tint = FinoraColors.Negative,
                modifier = Modifier.size(16.dp),
            )
            AppText(
                text = message.orEmpty(),
                size = 13,
                weight = AppFontWeight.SemiBold,
                color = FinoraColors.Negative,
            )
        }
    }
}

/** Full-width capsule primary action (iOS `FormPrimaryButton`). */
@Composable
fun FormPrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: CustomColor = CustomColor.Accent,
    isEnabled: Boolean = true,
) {
    val alpha by animateFloatAsState(if (isEnabled) 1f else 0.45f, tween(200), label = "primary-alpha")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .alpha(alpha)
            .shadow(12.dp, CircleShape, ambientColor = tint.color.copy(alpha = 0.4f), spotColor = tint.color.copy(alpha = 0.4f))
            .clip(CircleShape)
            .background(tint.color)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = title,
            size = 20,
            weight = AppFontWeight.ExtraBold,
            color = FinoraColors.OnAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Card padding + surface (iOS `.sectionCard()`). */
fun Modifier.sectionCard(padding: androidx.compose.ui.unit.Dp = 20.dp): Modifier =
    this
        .fillMaxWidth()
        .finoraCard()
        .padding(padding)

/** Small upper-case section caption (iOS `SectionLabel`). */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    AppText(
        text = text.uppercase(),
        size = 12,
        weight = AppFontWeight.SemiBold,
        color = FinoraColors.TextSecondary,
        letterSpacing = 1.sp,
        maxLines = 1,
        modifier = modifier,
    )
}

/** Title over a bold value (iOS `StatColumn`); expands to share a Row equally via [Modifier.weight]. */
@Composable
fun StatColumn(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = FinoraColors.TextPrimary,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        AppText(text = title, size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary)
        ShrinkToFitText(
            text = value,
            style = appTextStyle(size = 15, weight = AppFontWeight.Bold),
            color = color,
            minScale = 0.6f,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Centered icon + title + message inside a card (iOS `EmptyStateCard`). */
@Composable
fun EmptyStateCard(
    icon: String,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = sfSymbolIcon(icon),
            contentDescription = null,
            tint = FinoraColors.TextTertiary,
            modifier = Modifier.size(44.dp),
        )
        AppText(text = title, size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
        AppText(
            text = message,
            size = 14,
            weight = AppFontWeight.Medium,
            color = FinoraColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

/** Convenience so callers can build a section card body without repeating the modifier chain. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    verticalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content,
    )
}
