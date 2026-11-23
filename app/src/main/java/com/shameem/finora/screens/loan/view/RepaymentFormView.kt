package com.shameem.finora.screens.loan.view

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
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AmountChip
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.viewmodel.RepaymentFormViewModel
import com.shameem.finora.screens.transaction.AccountDropdown
import java.time.Instant

@Composable
fun RepaymentFormView(form: RepaymentFormViewModel, onDismiss: () -> Unit) {
    val kind = form.kind
    val focusManager = LocalFocusManager.current

    FormSheet(
        title = kind.repaymentActionTitle,
        primaryTitle = kind.repaymentActionTitle,
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmountChip(
                        title = "Full amount · ${CurrencyFormatter.taka(form.outstanding, showFraction = true)}",
                        tint = kind.tint.color,
                        onClick = {
                            focusManager.clearFocus()
                            form.payInFull()
                        },
                    )
                    form.nextInstallment?.let { next ->
                        AmountChip(
                            title = "Next installment · ${CurrencyFormatter.taka(next.remaining, showFraction = true)}",
                            tint = kind.tint.color,
                            onClick = {
                                focusManager.clearFocus()
                                form.payNextInstallment()
                            },
                        )
                    }
                }
            },
        )
        Column(Modifier.fillMaxWidth().finoraCard().padding(16.dp)) {
            AccountDropdown(
                title = kind.repaymentAccountTitle,
                accounts = form.accounts,
                selection = form.accountId,
                onSelect = { form.accountId = it },
                excluded = null,
            )
        }
        Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
            FormFieldRow(icon = "calendar", title = "Date") {
                DateField(date = form.date, onDateChange = { form.date = it }, min = form.earliestDate, max = Instant.now())
            }
            HorizontalDivider(color = FinoraColors.Divider)
            FormFieldRow(icon = "text.alignleft", title = "Note (optional)", alignTop = true) {
                FormTextEntry(
                    value = form.note,
                    onValueChange = { form.note = it },
                    placeholder = "e.g. Cash handed over",
                    maxLines = 3,
                )
            }
        }
        Breakdown(form)
        FormErrorBanner(message = form.visibleError)
    }
}

@Composable
private fun HeaderCard(form: RepaymentFormViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(16.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PersonAvatar(form.item.person, size = 46.dp)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(form.item.person.name, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
            AppText(
                "${form.kind.outstandingCaption}: ${CurrencyFormatter.taka(form.outstanding, showFraction = true)}",
                size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun Breakdown(form: RepaymentFormViewModel) {
    val remaining = form.remainingAfter ?: return
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 22.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ValueRow("Amount", CurrencyFormatter.taka(form.amount, showFraction = true))
        HorizontalDivider(color = FinoraColors.Divider)
        ValueRow(
            if (remaining < Loan.TOLERANCE) "Fully settled" else "Remaining after",
            CurrencyFormatter.taka(remaining, showFraction = true),
            emphasized = true,
        )
        val after = form.balanceAfter
        val account = form.account
        if (after != null && account != null) {
            ValueRow("${account.name} balance after", CurrencyFormatter.taka(after, showFraction = true))
        }
    }
}
