package com.shameem.finora.screens.cashflow.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AmountField
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ConfirmDeleteDialog
import com.shameem.finora.core.ui.DateField
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.cashflow.model.accountTitle
import com.shameem.finora.screens.cashflow.model.tint
import com.shameem.finora.screens.cashflow.viewmodel.CashFlowFormViewModel
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.transaction.AccountDropdown
import java.time.Instant

private val ChipMinWidth = 88.dp
private val ChipSpacing = 10.dp

@Composable
fun CashFlowFormView(
    form: CashFlowFormViewModel,
    onDismiss: () -> Unit,
) {
    val kind = form.kind
    var confirmDelete by remember { mutableStateOf(false) }

    FormSheet(
        title = form.title,
        primaryTitle = form.saveTitle,
        tint = kind.tint,
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        AmountField(title = "Amount", text = form.amountText, onTextChange = { form.amountText = it })
        CategoryCard(form)
        AccountDropdown(
            title = kind.accountTitle,
            accounts = form.accounts,
            selection = form.accountId,
            onSelect = { form.accountId = it },
            excluded = null,
            modifier = Modifier.fillMaxWidth().finoraCard().padding(16.dp),
        )
        DetailsCard(form)
        form.balanceAfter?.let { after ->
            form.account?.let { account ->
                ValueRow(
                    title = "${account.name} balance after",
                    value = CurrencyFormatter.taka(after, showFraction = true),
                    emphasized = true,
                    modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 22.dp).padding(16.dp),
                )
            }
        }
        FormErrorBanner(message = form.visibleError)
        if (form.isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button) { confirmDelete = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                AppText("Delete ${kind.title}", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
            }
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete this ${kind.singular}?",
            message = "The account balance is adjusted back.",
            onConfirm = {
                confirmDelete = false
                form.delete()
                onDismiss()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun CategoryCard(form: CashFlowFormViewModel) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("Category")
        if (form.hasCategories) {
            // Adaptive grid: as many 88dp-minimum columns as fit.
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val columns = maxOf(1, ((maxWidth + ChipSpacing) / (ChipMinWidth + ChipSpacing)).toInt())
                Column(verticalArrangement = Arrangement.spacedBy(ChipSpacing)) {
                    form.categories.chunked(columns).forEach { rowItems ->
                        Row(horizontalArrangement = Arrangement.spacedBy(ChipSpacing)) {
                            rowItems.forEach { category ->
                                CategoryChip(
                                    category = category,
                                    selected = category.id == form.categoryId,
                                    onClick = { form.categoryId = category.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        } else {
            AppText(
                text = "You don't have any ${form.kind.singular} categories yet. Add one from the Categories tab first.",
                size = 13,
                weight = AppFontWeight.Medium,
                color = FinoraColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: TransactionCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = remember(category.tint) { colorFromHex(category.tint) }
    val chipShape = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(chipShape)
            .background(if (selected) tint.copy(alpha = 0.12f) else Color.Transparent, chipShape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = category.name
                this.selected = selected
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (selected) tint else tint.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = sfSymbolIcon(category.icon),
                contentDescription = null,
                tint = if (selected) Color.White else tint,
                modifier = Modifier.size(20.dp),
            )
        }
        AppText(
            text = category.name,
            size = 11,
            weight = if (selected) AppFontWeight.Bold else AppFontWeight.SemiBold,
            color = FinoraColors.TextPrimary,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun DetailsCard(form: CashFlowFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 16.dp),
    ) {
        FormFieldRow(icon = "calendar", title = "Date") {
            DateField(
                date = form.date,
                onDateChange = { form.date = it },
                max = Instant.now(),
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "text.alignleft", title = "Note (optional)", alignTop = true) {
            FormTextEntry(
                value = form.note,
                onValueChange = { form.note = it },
                placeholder = if (form.kind == CategoryKind.Income) "e.g. November salary" else "e.g. Lunch with team",
                capitalization = KeyboardCapitalization.Sentences,
                maxLines = 3,
            )
        }
    }
}
