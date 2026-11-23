package com.shameem.finora.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors

/** Right-aligned single-line text input with a placeholder, used inside [FormFieldRow]. */
@Composable
fun FormTextEntry(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Next,
    /** 1 keeps the field on a single line; more lets it wrap and grow up to that many lines. */
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Box(modifier.widthIn(min = 120.dp), contentAlignment = Alignment.CenterEnd) {
        if (value.isEmpty()) {
            AppText(
                text = placeholder,
                size = 15,
                weight = AppFontWeight.SemiBold,
                color = FinoraColors.TextTertiary,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = maxLines == 1,
            maxLines = maxLines,
            textStyle = FormFieldTextStyle.copy(textAlign = TextAlign.End),
            cursorBrush = SolidColor(FinoraColors.TextPrimary),
            keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = imeAction, keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
