package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.StatColumn
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.savings.model.SavingsLogFilter
import com.shameem.finora.screens.savings.model.SavingsLogSection
import com.shameem.finora.screens.savings.viewmodel.SavingsLogViewModelProtocol
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/** The log shown as a sheet: title bar with "Done", then the log itself. */
@Composable
fun SavingsLogSheet(
    model: SavingsLogViewModelProtocol,
    onDismiss: () -> Unit,
) {
    val state by model.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().background(FinoraColors.ScreenBackground)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText(
                state.scopeTitle ?: "Savings Log",
                size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary,
                maxLines = 1, modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onDismiss) {
                AppText("Done", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
            }
        }
        SavingsLogView(model)
    }
}

@Composable
fun SavingsLogView(
    model: SavingsLogViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "filter") {
            PillPicker(options = SavingsLogFilter.entries, selection = state.filter, onSelectionChange = model::setFilter)
        }
        item(key = "totals") {
            Row(Modifier.sectionCard()) {
                StatColumn("Added", CurrencyFormatter.taka(state.totalAdded, showFraction = true), Modifier.weight(1f), FinoraColors.Positive)
                StatColumn("Withdrawn", CurrencyFormatter.taka(state.totalWithdrawn, showFraction = true), Modifier.weight(1f), FinoraColors.Negative)
                StatColumn("Net", CurrencyFormatter.taka(state.totalAdded - state.totalWithdrawn, showFraction = true), Modifier.weight(1f))
            }
        }
        if (state.sections.isEmpty()) {
            item(key = "empty") {
                EmptyStateCard(
                    icon = "clock.arrow.circlepath",
                    title = if (state.isEmpty) "No activity yet" else "Nothing to show",
                    message = if (state.isEmpty) "Every amount you add or withdraw will be listed here." else "Try a different filter.",
                )
            }
        } else {
            items(state.sections, key = { it.month.toString() }) { section ->
                SectionCard(section, showSavingsName = state.scopeTitle == null)
            }
        }
    }
}

@Composable
private fun SectionCard(section: SavingsLogSection, showSavingsName: Boolean) {
    val month = "${section.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${section.month.year}"
    Column(Modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionLabel(month, Modifier.weight(1f))
            AppText(
                "Net ${if (section.net < 0) "−" else ""}${CurrencyFormatter.taka(abs(section.net))}",
                size = 12, weight = AppFontWeight.Bold, color = FinoraColors.TextSecondary,
            )
        }
        section.entries.forEachIndexed { index, log ->
            SavingsEntryRow(log.entry, savingsName = if (showSavingsName) log.savingsName else null)
            if (index != section.entries.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
        }
    }
}
