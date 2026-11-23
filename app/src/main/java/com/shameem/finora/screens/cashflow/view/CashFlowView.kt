package com.shameem.finora.screens.cashflow.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.Palette
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.DonutCard
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.cashflow.model.CashFlowItem
import com.shameem.finora.screens.cashflow.model.addTitle
import com.shameem.finora.screens.cashflow.model.centerCaption
import com.shameem.finora.screens.cashflow.model.screenTitle
import com.shameem.finora.screens.cashflow.model.tint
import com.shameem.finora.screens.cashflow.model.totalTitle
import com.shameem.finora.screens.cashflow.viewmodel.CashFlowFormViewModel
import com.shameem.finora.screens.cashflow.viewmodel.CashFlowViewModelProtocol
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.view.CategoryIconBadge
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.services.date.shortDisplay

/** Income or expense overview: period filter, category donut and the list of entries. */
@Composable
fun CashFlowView(
    model: CashFlowViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<CashFlowFormViewModel?>(null) }
    val kind = model.kind

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AddActionButton(
                title = kind.addTitle,
                tint = kind.tint,
                onClick = { form = model.makeFormViewModel(null) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenTitle(kind.screenTitle)
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PillPicker(
                    options = ReportPeriod.entries,
                    selection = state.period,
                    onSelectionChange = model::setPeriod,
                )
                if (state.items.isEmpty()) {
                    EmptyStateCard(
                        icon = if (kind == CategoryKind.Income) "arrow.down.circle" else "arrow.up.circle",
                        title = "No ${kind.singular} yet",
                        message = "Nothing recorded for ${state.period.subtitle().lowercase()}. " +
                            "Tap “${kind.addTitle}” to add one.",
                    )
                } else {
                    DonutCard(
                        title = kind.totalTitle,
                        subtitle = state.period.subtitle(),
                        centerCaption = kind.centerCaption,
                        items = state.items,
                    )
                    EntriesCard(
                        kind = kind,
                        entries = state.entries,
                        onEdit = { form = model.makeFormViewModel(it.entry) },
                    )
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) {
            CashFlowFormView(form = current, onDismiss = { form = null })
        }
    }
}

@Composable
private fun EntriesCard(
    kind: CategoryKind,
    entries: List<CashFlowItem>,
    onEdit: (CashFlowItem) -> Unit,
) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel(if (kind == CategoryKind.Income) "Recent income" else "Recent expenses")
        entries.forEachIndexed { index, item ->
            CashFlowRow(item = item, onClick = { onEdit(item) })
            if (index != entries.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
        }
    }
}

@Composable
fun CashFlowRow(
    item: CashFlowItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = item.entry
    val isIncome = entry.kind == CategoryKind.Income
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { onClick(label = "Edit") { onClick(); true } },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CategoryIconBadge(
            icon = item.category?.icon ?: "questionmark.circle",
            tint = item.category?.tint ?: Palette.secondaryHex,
            size = 40.dp,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = item.category?.name ?: "Uncategorised",
                size = 14,
                weight = AppFontWeight.Bold,
                color = FinoraColors.TextPrimary,
                maxLines = 1,
            )
            AppText(
                text = "${item.account?.name ?: "Account removed"} · ${entry.date.shortDisplay()}",
                size = 12,
                weight = AppFontWeight.Medium,
                color = FinoraColors.TextSecondary,
                maxLines = 1,
            )
            if (entry.note.isNotEmpty()) {
                AppText(
                    text = entry.note,
                    size = 12,
                    weight = AppFontWeight.Medium,
                    color = FinoraColors.TextTertiary,
                    maxLines = 1,
                )
            }
        }
        ShrinkToFitText(
            text = (if (isIncome) "+" else "−") + CurrencyFormatter.taka(entry.amount, showFraction = true),
            style = appTextStyle(size = 15, weight = AppFontWeight.ExtraBold),
            color = if (isIncome) FinoraColors.Positive else FinoraColors.Negative,
            minScale = 0.7f,
            modifier = Modifier.widthIn(max = 140.dp),
            alignment = Alignment.CenterEnd,
        )
    }
}
