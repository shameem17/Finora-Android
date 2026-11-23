package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FinoraSwitch
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormFieldTextStyle
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.LogoColorPickerCard
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.setLogo
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.savings.model.SavingsItem
import com.shameem.finora.screens.savings.viewmodel.SavingsFormViewModel
import com.shameem.finora.screens.transaction.AccountDropdown
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Instant

@Composable
fun SavingsFormView(
    form: SavingsFormViewModel,
    onDismiss: () -> Unit,
) {
    FormSheet(
        title = form.title,
        primaryTitle = form.saveTitle,
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        SavingsCard(
            item = SavingsItem(form.draft, form.account?.let(::AccountSummary)),
            modifier = Modifier.clearAndSetSemantics { },
        )
        AccountCard(form)
        DetailsCard(form)
        GoalCard(form)
        if (!form.isEditing) {
            AmountField(
                title = "Already saved (optional)",
                text = form.openingText,
                onTextChange = { form.openingText = it },
                fontSize = 34,
                accessory = {
                    AppText(
                        "Money you've already put aside. It won't be deducted from any account.",
                        size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                    )
                },
            )
        }
        LogoColorPickerCard(
            tintHex = form.tintHex,
            onTintChange = { form.tintHex = it },
            hasLogo = form.hasLogo,
            isProcessing = form.isProcessingLogo,
            onPickImage = { form.setLogo(it) },
            onRemove = form::removeLogo,
        )
    }
}

@Composable
private fun AccountCard(form: SavingsFormViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AccountDropdown(
            title = "Tagged account",
            accounts = form.accounts,
            selection = form.accountId,
            onSelect = form::selectAccount,
            excluded = null,
        )
        AppText(
            "Deposits are taken from, and withdrawals returned to, accounts you choose each time. This one is the default.",
            size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
        )
    }
}

@Composable
private fun DetailsCard(form: SavingsFormViewModel) {
    Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
        FormFieldRow(icon = "textformat", title = "Name") {
            FormTextEntry(
                value = form.name,
                onValueChange = form::onNameChange,
                placeholder = "e.g. City Bank Savings",
                capitalization = KeyboardCapitalization.Words,
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "number", title = "Account no.") {
            FormTextEntry(
                value = form.number,
                onValueChange = { form.number = it },
                placeholder = "Optional",
                capitalization = KeyboardCapitalization.None,
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "calendar", title = "Start date") {
            DateField(date = form.startDate, onDateChange = { form.startDate = it }, max = Instant.now())
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "text.alignleft", title = "Note", alignTop = true) {
            FormTextEntry(
                value = form.note,
                onValueChange = { form.note = it },
                placeholder = "Optional",
                capitalization = KeyboardCapitalization.Sentences,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun GoalCard(form: SavingsFormViewModel) {
    Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
        FormFieldRow(icon = "flag.checkered", title = "Target") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    CurrencyFormatter.symbol, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextTertiary,
                )
                Box(Modifier.widthIn(min = 60.dp, max = 130.dp), contentAlignment = Alignment.CenterEnd) {
                    if (form.targetText.isEmpty()) {
                        AppText(
                            "Optional", size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextTertiary,
                            textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    BasicTextField(
                        value = form.targetText,
                        onValueChange = { form.targetText = DecimalInput.sanitize(it) },
                        singleLine = true,
                        textStyle = FormFieldTextStyle.copy(textAlign = TextAlign.End),
                        cursorBrush = SolidColor(FinoraColors.TextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "calendar.badge.clock", title = "Set a target date") {
            FinoraSwitch(checked = form.hasTargetDate, onCheckedChange = { form.hasTargetDate = it })
        }
        if (form.hasTargetDate) {
            HorizontalDivider(color = FinoraColors.Divider)
            FormFieldRow(icon = "flag.fill", title = "Target date") {
                DateField(date = form.targetDate, onDateChange = { form.targetDate = it }, min = form.startDate)
            }
        }
    }
}
