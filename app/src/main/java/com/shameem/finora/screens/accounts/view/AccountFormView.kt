package com.shameem.finora.screens.accounts.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormFieldTextStyle
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.LogoColorPickerCard
import com.shameem.finora.core.ui.MenuPicker
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.setLogo
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.accounts.viewmodel.AccountFormViewModel
import com.shameem.finora.screens.home.view.AccountCard
import com.shameem.finora.screens.home.view.AccountType

@Composable
fun AccountFormView(
    form: AccountFormViewModel,
    onDismiss: () -> Unit,
) {
    FormSheet(
        title = form.title,
        primaryTitle = if (form.isEditing) "Save Changes" else "Save Account",
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        val glow = colorFromHex(form.tintHex).copy(alpha = 0.35f)
        AccountCard(
            account = form.draft,
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = glow, spotColor = glow)
                .clearAndSetSemantics { },
        )
        DetailsCard(form)
        LogoColorPickerCard(
            tintHex = form.tintHex,
            onTintChange = { form.tintHex = it },
            hasLogo = form.hasLogo,
            isProcessing = form.isProcessingLogo,
            onPickImage = { form.setLogo(it) },
            onRemove = form::removeLogo,
        )
        AmountField(
            title = "Initial balance",
            text = form.initialBalanceText,
            onTextChange = { form.initialBalanceText = it },
            fontSize = 34,
        )
        CurrentBalanceCard(form)
    }
}

@Composable
private fun DetailsCard(form: AccountFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 16.dp),
    ) {
        FormFieldRow(icon = "textformat", title = "Name") {
            TextEntry(
                value = form.name,
                onValueChange = { form.name = it },
                placeholder = "e.g. City Bank",
                capitalization = KeyboardCapitalization.Words,
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "number", title = "Account no.") {
            TextEntry(
                value = form.number,
                onValueChange = { form.number = it },
                placeholder = "Optional",
                capitalization = KeyboardCapitalization.None,
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "square.grid.2x2", title = "Type") {
            MenuPicker(
                options = AccountType.selectable,
                selection = form.type,
                onSelect = { form.type = it },
                title = { it.title },
                icon = { it.symbol },
            )
        }
    }
}

/** Right-aligned single-line text input with a placeholder, used inside [FormFieldRow]. */
@Composable
private fun TextEntry(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    capitalization: KeyboardCapitalization,
) {
    Box(Modifier.widthIn(min = 120.dp), contentAlignment = Alignment.CenterEnd) {
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
            singleLine = true,
            textStyle = FormFieldTextStyle.copy(textAlign = TextAlign.End),
            cursorBrush = SolidColor(FinoraColors.TextPrimary),
            keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CurrentBalanceCard(form: AccountFormViewModel) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    sfSymbolIcon("lock.fill"),
                    contentDescription = null,
                    tint = FinoraColors.TextSecondary,
                    modifier = Modifier.size(16.dp),
                )
                AppText(
                    text = "Current balance",
                    size = 14,
                    weight = AppFontWeight.SemiBold,
                    color = FinoraColors.TextSecondary,
                )
            }
            AppText(
                text = CurrencyFormatter.taka(form.currentBalance, showFraction = true),
                size = 16,
                weight = AppFontWeight.ExtraBold,
                color = FinoraColors.TextPrimary,
            )
        }
        AppText(
            text = if (form.isEditing) {
                "Calculated from your initial balance plus every transfer, loan, savings and payment. It can't be edited directly."
            } else {
                "Starts equal to the initial balance, then updates automatically as you record activity."
            },
            size = 12,
            weight = AppFontWeight.Medium,
            color = FinoraColors.TextSecondary,
        )
    }
}
