package com.shameem.finora.screens.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormFieldTextStyle
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import java.time.Instant
import java.util.UUID

@Composable
fun TransferFormView(
    form: TransferFormViewModel,
    onDismiss: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    FormSheet(
        title = "New Transfer",
        primaryTitle = "Save Transfer",
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        AccountsCard(form = form, onSwap = {
            form.swapAccounts()
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        })
        AmountField(title = "Amount", text = form.amountText, onTextChange = { form.amountText = it })
        DetailsCard(form)
        Breakdown(form)
        FormErrorBanner(message = form.visibleError)
    }
}

@Composable
private fun AccountsCard(form: TransferFormViewModel, onSwap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(16.dp),
    ) {
        AccountDropdown(
            title = "From",
            accounts = form.accounts,
            selection = form.fromId,
            onSelect = { form.fromId = it },
            excluded = form.toId,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HorizontalDivider(Modifier.weight(1f), color = FinoraColors.FillSubtle)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(FinoraColors.Accent.copy(alpha = 0.25f), CircleShape)
                    .semantics { contentDescription = "Swap accounts" }
                    .clickable(role = Role.Button, onClick = onSwap),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    sfSymbolIcon("arrow.up.arrow.down"),
                    contentDescription = null,
                    tint = FinoraColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
            HorizontalDivider(Modifier.weight(1f), color = FinoraColors.FillSubtle)
        }
        AccountDropdown(
            title = "To",
            accounts = form.accounts,
            selection = form.toId,
            onSelect = { form.toId = it },
            excluded = form.fromId,
        )
    }
}

@Composable
private fun DetailsCard(form: TransferFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 16.dp),
    ) {
        FormFieldRow(icon = "percent", title = "Transaction charge") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    CurrencyFormatter.symbol,
                    size = 16,
                    weight = AppFontWeight.Bold,
                    color = FinoraColors.TextTertiary,
                )
                Box(Modifier.widthIn(max = 110.dp)) {
                    if (form.chargeText.isEmpty()) {
                        AppText(
                            "0",
                            size = 16,
                            weight = AppFontWeight.Bold,
                            color = FinoraColors.TextTertiary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    BasicTextField(
                        value = form.chargeText,
                        onValueChange = { form.chargeText = DecimalInput.sanitize(it) },
                        singleLine = true,
                        textStyle = appTextStyle(16, AppFontWeight.Bold)
                            .copy(color = FinoraColors.TextPrimary, textAlign = TextAlign.End),
                        cursorBrush = SolidColor(FinoraColors.TextPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.widthIn(min = 40.dp, max = 110.dp),
                    )
                }
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "calendar", title = "Date") {
            DateField(
                date = form.date,
                onDateChange = { form.date = it },
                max = Instant.now(),
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "text.alignleft", title = "Reason (optional)", alignTop = true) {
            Box(Modifier.weight(1f, fill = false).widthIn(min = 120.dp)) {
                if (form.reason.isEmpty()) {
                    AppText(
                        "e.g. Rent, top-up…",
                        size = 15,
                        weight = AppFontWeight.SemiBold,
                        color = FinoraColors.TextTertiary,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BasicTextField(
                    value = form.reason,
                    onValueChange = { form.reason = it },
                    maxLines = 3,
                    textStyle = FormFieldTextStyle.copy(textAlign = TextAlign.End),
                    cursorBrush = SolidColor(FinoraColors.TextPrimary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Breakdown(form: TransferFormViewModel) {
    val after = form.balanceAfter
    AnimatedVisibility(
        visible = after != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .finoraCard(cornerRadius = 22.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ValueRow("Amount", CurrencyFormatter.taka(form.amount, showFraction = true))
            ValueRow("Charge", CurrencyFormatter.taka(form.charge, showFraction = true))
            HorizontalDivider(color = FinoraColors.Divider)
            ValueRow("Total deducted", CurrencyFormatter.taka(form.totalDebit, showFraction = true), emphasized = true)
            ValueRow(
                title = "${form.fromAccount?.name ?: "Source"} balance after",
                value = CurrencyFormatter.taka(after ?: 0.0, showFraction = true),
            )
        }
    }
}

/** Tappable account picker showing the chosen account's badge, name and balance (iOS `AccountDropdown`). */
@Composable
fun AccountDropdown(
    title: String,
    accounts: List<Account>,
    selection: UUID?,
    onSelect: (UUID) -> Unit,
    excluded: UUID?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selection }

    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "$title account, ${selected?.name ?: "None selected"}" }
                .clickable(role = Role.DropdownList) { expanded = true }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (selected != null) {
                AccountBadge(type = selected.type, tint = selected.tint, size = 44.dp)
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(FinoraColors.FillSubtle, RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        sfSymbolIcon("plus"),
                        contentDescription = null,
                        tint = FinoraColors.TextTertiary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(
                    text = title.uppercase(),
                    size = 11,
                    weight = AppFontWeight.SemiBold,
                    color = FinoraColors.TextSecondary,
                )
                AppText(
                    text = selected?.name ?: "Select account",
                    size = 16,
                    weight = AppFontWeight.Bold,
                    color = FinoraColors.TextPrimary,
                    maxLines = 1,
                )
                if (selected != null) {
                    AppText(
                        text = "Balance " + CurrencyFormatter.taka(selected.balance, showFraction = true),
                        size = 12,
                        weight = AppFontWeight.Medium,
                        color = FinoraColors.TextSecondary,
                    )
                }
            }
            Icon(
                sfSymbolIcon("chevron.up.chevron.down"),
                contentDescription = null,
                tint = FinoraColors.TextTertiary,
                modifier = Modifier.size(18.dp),
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        AppText(
                            text = "${account.name} · ${CurrencyFormatter.taka(account.balance, showFraction = true)}",
                            size = 15,
                            weight = AppFontWeight.SemiBold,
                            color = FinoraColors.TextPrimary,
                        )
                    },
                    leadingIcon = if (account.id == selection) {
                        { Icon(sfSymbolIcon("checkmark"), contentDescription = null) }
                    } else {
                        null
                    },
                    enabled = account.id != excluded,
                    onClick = {
                        onSelect(account.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
