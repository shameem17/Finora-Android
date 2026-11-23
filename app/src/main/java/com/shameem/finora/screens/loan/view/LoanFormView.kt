package com.shameem.finora.screens.loan.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FinoraSwitch
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.viewmodel.LoanFormViewModel
import com.shameem.finora.screens.transaction.AccountBadge
import com.shameem.finora.screens.transaction.AccountDropdown
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Instant

@Composable
fun LoanFormView(form: LoanFormViewModel, onDismiss: () -> Unit) {
    var showPersonPicker by remember { mutableStateOf(false) }

    FormSheet(
        title = form.title,
        primaryTitle = form.saveTitle,
        tint = form.kind.tint,
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        if (!form.isEditing) {
            PillPicker(LoanKind.entries, form.kind, onSelectionChange = { form.kind = it })
        }
        PartyCard(form, onPickPerson = { showPersonPicker = true })
        if (form.isEditing) LockedAmountCard(form) else {
            AmountField(title = "Amount", text = form.amountText, onTextChange = { form.amountText = it })
        }
        DetailsCard(form)
        Breakdown(form)
        FormErrorBanner(message = form.visibleError)
    }

    if (showPersonPicker) {
        FormSheetHost(onDismissRequest = { showPersonPicker = false }) {
            PersonPickerView(form, onDismiss = { showPersonPicker = false })
        }
    }
}

@Composable
private fun PartyCard(form: LoanFormViewModel, onPickPerson: () -> Unit) {
    val focusManager = LocalFocusManager.current
    Column(Modifier.fillMaxWidth().finoraCard().padding(16.dp)) {
        PersonRow(form, onClick = {
            focusManager.clearFocus()
            onPickPerson()
        })
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = FinoraColors.Divider)
        val locked = form.lockedAccount
        if (locked != null) {
            LockedAccountRow(form.kind, locked)
        } else {
            AccountDropdown(
                title = form.kind.accountFieldTitle,
                accounts = form.accounts,
                selection = form.accountId,
                onSelect = { form.accountId = it },
                excluded = null,
            )
        }
    }
}

@Composable
private fun PersonRow(form: LoanFormViewModel, onClick: () -> Unit) {
    val person = form.person
    val role = form.kind.personRole
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (person != null) {
            PersonAvatar(person, size = 44.dp)
        } else {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(FinoraColors.FillSubtle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(sfSymbolIcon("plus"), null, tint = FinoraColors.TextTertiary, modifier = Modifier.size(18.dp))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            SectionLabel(role)
            AppText(
                person?.name ?: "Select ${role.lowercase()}",
                size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1,
            )
            if (person != null) {
                AppText(
                    person.phone ?: person.category.title,
                    size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1,
                )
            }
        }
        Icon(sfSymbolIcon("chevron.right"), null, tint = FinoraColors.TextTertiary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun LockedAccountRow(kind: LoanKind, account: AccountSummary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AccountBadge(type = account.type, tint = account.tint, size = 44.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            SectionLabel(kind.accountFieldTitle)
            AppText(account.name, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
        }
        Icon(sfSymbolIcon("lock.fill"), null, tint = FinoraColors.TextTertiary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun LockedAmountCard(form: LoanFormViewModel) {
    Column(Modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("Amount", Modifier.weight(1f))
            Icon(sfSymbolIcon("lock.fill"), null, tint = FinoraColors.TextTertiary, modifier = Modifier.size(12.dp))
            AppText("  Locked", size = 11, weight = AppFontWeight.Bold, color = FinoraColors.TextTertiary)
        }
        AppText(
            CurrencyFormatter.taka(form.amount, showFraction = true),
            size = 40, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextSecondary, maxLines = 1,
        )
        AppText(
            "To change the amount or account, delete this entry and add it again.",
            size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
        )
    }
}

@Composable
private fun DetailsCard(form: LoanFormViewModel) {
    Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
        FormFieldRow(icon = "percent", title = "Interest / extra charge") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(CurrencyFormatter.symbol, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextTertiary)
                FormTextEntry(
                    value = form.interestText,
                    onValueChange = { form.interestText = DecimalInput.sanitize(it) },
                    placeholder = "0",
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.width(110.dp),
                )
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "calendar", title = "Date") {
            DateField(date = form.date, onDateChange = form::onLoanDateChanged, max = Instant.now())
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "calendar.badge.clock", title = "Set a due date") {
            FinoraSwitch(checked = form.hasDueDate, onCheckedChange = { form.hasDueDate = it })
        }
        if (form.hasDueDate) {
            HorizontalDivider(color = FinoraColors.Divider)
            FormFieldRow(icon = "flag.fill", title = if (form.installmentCount > 1) "First due date" else "Due date") {
                DateField(date = form.dueDate, onDateChange = { form.dueDate = it }, min = form.date)
            }
            HorizontalDivider(color = FinoraColors.Divider)
            FormFieldRow(icon = "list.number", title = "Monthly installments") {
                Stepper(
                    value = form.installmentCount,
                    range = 1..Loan.MAX_INSTALLMENTS,
                    onValueChange = { form.installmentCount = it },
                )
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "text.alignleft", title = "Note (optional)", alignTop = true) {
            FormTextEntry(
                value = form.note,
                onValueChange = { form.note = it },
                placeholder = "e.g. Rent, shop stock…",
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun Breakdown(form: LoanFormViewModel) {
    if (form.amount <= 0) return
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 22.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ValueRow("Amount", CurrencyFormatter.taka(form.amount, showFraction = true))
        if (form.interest > 0) {
            ValueRow("Interest / extra", CurrencyFormatter.taka(form.interest, showFraction = true))
        }
        HorizontalDivider(color = FinoraColors.Divider)
        ValueRow(
            if (form.kind == LoanKind.Lent) "Total to receive" else "Total to pay",
            CurrencyFormatter.taka(form.totalPayable, showFraction = true),
            emphasized = true,
        )
        form.installmentAmount?.let {
            ValueRow("${form.installmentCount} monthly installments of", "≈ " + CurrencyFormatter.taka(it, showFraction = true))
        }
        val after = form.balanceAfter
        val account = form.account
        if (after != null && account != null) {
            ValueRow("${account.name} balance after", CurrencyFormatter.taka(after, showFraction = true))
        }
    }
}

/** − value + control (iOS `Stepper`). */
@Composable
fun Stepper(value: Int, range: IntRange, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = { onValueChange((value - 1).coerceIn(range)) }, enabled = value > range.first) {
            Icon(
                sfSymbolIcon("minus"),
                contentDescription = "Decrease",
                tint = if (value > range.first) FinoraColors.TextPrimary else FinoraColors.TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
        AppText("$value", size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, modifier = Modifier.width(28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        IconButton(onClick = { onValueChange((value + 1).coerceIn(range)) }, enabled = value < range.last) {
            Icon(
                sfSymbolIcon("plus"),
                contentDescription = "Increase",
                tint = if (value < range.last) FinoraColors.TextPrimary else FinoraColors.TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
