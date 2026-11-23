package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AmountChip
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FinoraSwitch
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.viewmodel.SavingsEntryFormViewModel
import com.shameem.finora.screens.transaction.AccountDropdown
import java.time.Instant
import kotlin.math.max

@Composable
fun SavingsEntryFormView(
    form: SavingsEntryFormViewModel,
    onDismiss: () -> Unit,
) {
    val kind = form.kind
    val focusManager = LocalFocusManager.current

    FormSheet(
        title = form.title,
        primaryTitle = form.saveTitle,
        tint = kind.tint,
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        HeaderCard(form)
        AmountField(
            title = "Amount",
            text = form.amountText,
            onTextChange = { form.amountText = it },
            accessory = {
                if (kind == SavingsEntryKind.Withdrawal) {
                    AmountChip(
                        title = "Break all · ${CurrencyFormatter.taka(form.current, showFraction = true)}",
                        tint = kind.tint.color,
                        onClick = {
                            focusManager.clearFocus()
                            form.withdrawAll()
                        },
                    )
                }
            },
        )
        AccountCard(form)
        DetailsCard(form)
        Breakdown(form)
        FormErrorBanner(message = form.visibleError)
    }
}

@Composable
private fun HeaderCard(form: SavingsEntryFormViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().finoraCard().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SavingsLogo(tint = form.savings.tint, logoData = form.savings.logoData)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(form.savings.name, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
            AppText(
                "Saved: ${CurrencyFormatter.taka(form.current, showFraction = true)}",
                size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun AccountCard(form: SavingsEntryFormViewModel) {
    val kind = form.kind
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppText(
                    if (kind == SavingsEntryKind.Deposit) "Take from an account" else "Add to an account",
                    size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary,
                )
                AppText(accountCaption(form), size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
            }
            FinoraSwitch(
                checked = form.usesAccount,
                onCheckedChange = { form.usesAccount = it },
                enabled = form.accounts.isNotEmpty(),
            )
        }
        if (form.usesAccount) {
            AccountDropdown(
                title = kind.accountTitle,
                accounts = form.accounts,
                selection = form.accountId,
                onSelect = { form.accountId = it },
                excluded = null,
            )
        }
    }
}

private fun accountCaption(form: SavingsEntryFormViewModel): String = when {
    form.usesAccount -> "The chosen account's balance is updated."
    form.kind == SavingsEntryKind.Deposit -> "Recorded only. No account balance changes."
    else -> "Recorded only. The money isn't added to any account."
}

@Composable
private fun DetailsCard(form: SavingsEntryFormViewModel) {
    Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
        FormFieldRow(icon = "calendar", title = "Date") {
            DateField(date = form.date, onDateChange = { form.date = it }, min = form.earliestDate, max = Instant.now())
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "text.alignleft", title = "Note (optional)", alignTop = true) {
            FormTextEntry(
                value = form.note,
                onValueChange = { form.note = it },
                placeholder = if (form.kind == SavingsEntryKind.Deposit) "e.g. Monthly saving" else "e.g. Medical emergency",
                capitalization = KeyboardCapitalization.Sentences,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun Breakdown(form: SavingsEntryFormViewModel) {
    val after = form.savingsAfter ?: return
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 22.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ValueRow("Amount", CurrencyFormatter.taka(form.amount, showFraction = true))
        HorizontalDivider(color = FinoraColors.Divider)
        ValueRow("Savings after", CurrencyFormatter.taka(max(after, 0.0), showFraction = true), emphasized = true)
        val balance = form.balanceAfter
        val account = form.account
        if (balance != null && account != null) {
            ValueRow("${account.name} balance after", CurrencyFormatter.taka(balance, showFraction = true))
        }
    }
}
