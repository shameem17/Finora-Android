package com.shameem.finora.screens.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import com.shameem.finora.core.ui.ColorSwatchPicker
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormSheet
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.screens.categories.viewmodel.CategoryFormViewModel

private const val ICON_COLUMNS = 5

@Composable
fun CategoryFormView(
    form: CategoryFormViewModel,
    onDismiss: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    FormSheet(
        title = form.title,
        primaryTitle = if (form.isEditing) "Save Changes" else "Save Category",
        isEnabled = form.canSubmit,
        onDismiss = onDismiss,
        onSubmit = form::submit,
    ) {
        CategoryRow(form.draft, Modifier.clearAndSetSemantics { })
        NameCard(form)
        IconCard(form)
        ColorCard(form)
        if (form.supportsBudget) {
            AmountField(
                title = "Monthly budget (optional)",
                text = form.budgetText,
                onTextChange = { form.budgetText = it },
                fontSize = 34,
                accessory = {
                    AppText(
                        text = "Leave empty if you don't want a spending limit for this category.",
                        size = 12,
                        weight = AppFontWeight.Medium,
                        color = FinoraColors.TextSecondary,
                    )
                },
            )
        }
        FormErrorBanner(message = form.error)
        if (form.isEditing) DeleteButton(onClick = { confirmDelete = true })
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = FinoraColors.ScreenBackground,
            title = { AppText("Delete this category?", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
            text = { AppText("Existing transactions are not removed.", size = 14, color = FinoraColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    form.delete()
                    onDismiss()
                }) {
                    AppText("Delete", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    AppText("Cancel", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                }
            },
        )
    }
}

@Composable
private fun NameCard(form: CategoryFormViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 16.dp),
    ) {
        FormFieldRow(icon = "textformat", title = "Name") {
            FormTextEntry(
                value = form.name,
                onValueChange = {
                    form.name = it
                    form.clearError()
                },
                placeholder = "e.g. Groceries",
                capitalization = KeyboardCapitalization.Words,
            )
        }
    }
}

@Composable
private fun IconCard(form: CategoryFormViewModel) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("Icon")
        val tint = colorFromHex(form.tintHex)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            form.kind.icons.chunked(ICON_COLUMNS).forEach { rowIcons ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowIcons.forEach { symbol ->
                        IconCell(
                            symbol = symbol,
                            selected = symbol == form.icon,
                            tint = tint,
                            onClick = { form.icon = symbol },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Keep the last row's cells the same width as the others.
                    repeat(ICON_COLUMNS - rowIcons.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun IconCell(
    symbol: String,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .heightIn(min = 46.dp)
            .clip(shape)
            .background(if (selected) tint else FinoraColors.FillSubtle, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = symbol
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = sfSymbolIcon(symbol),
            contentDescription = null,
            tint = if (selected) Color.White else FinoraColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ColorCard(form: CategoryFormViewModel) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("Colour")
        ColorSwatchPicker(hex = form.tintHex, onHexChange = { form.tintHex = it })
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText("Delete Category", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
    }
}
