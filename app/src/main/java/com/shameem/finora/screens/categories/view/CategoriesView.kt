package com.shameem.finora.screens.categories.view

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.viewmodel.CategoriesViewModelProtocol
import com.shameem.finora.screens.categories.viewmodel.CategoryFormViewModel

@Composable
fun CategoriesView(
    model: CategoriesViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<CategoryFormViewModel?>(null) }

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AddActionButton(
                title = "Add ${state.kind.title} Category",
                onClick = { form = model.makeFormViewModel(null) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            ScreenTitle("Categories")
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(key = "kind-picker") {
                    PillPicker(
                        options = CategoryKind.entries,
                        selection = state.kind,
                        onSelectionChange = model::selectKind,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                if (state.kind == CategoryKind.Expense) {
                    item(key = "budget-summary") {
                        BudgetSummaryCard(
                            totalBudget = state.totalBudget,
                            budgeted = state.budgetedCount,
                            total = state.categories.size,
                        )
                    }
                }
                if (state.categories.isEmpty()) {
                    item(key = "empty") {
                        EmptyStateCard(
                            icon = "square.grid.2x2",
                            title = "No ${state.kind.singular} categories",
                            message = "Create one to organise your ${state.kind.singular}.",
                        )
                    }
                } else {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            modifier = Modifier
                                .animateContentSize()
                                .clickable(role = Role.Button) { form = model.makeFormViewModel(category) },
                        )
                    }
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) {
            CategoryFormView(form = current, onDismiss = { form = null })
        }
    }
}

@Composable
private fun BudgetSummaryCard(totalBudget: Double, budgeted: Int, total: Int) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SectionLabel("Total monthly budget")
        ShrinkToFitText(
            text = CurrencyFormatter.taka(totalBudget),
            style = appTextStyle(size = 30, weight = AppFontWeight.ExtraBold),
            color = FinoraColors.TextPrimary,
            minScale = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(
            text = "$budgeted of $total categories budgeted",
            size = 13,
            weight = AppFontWeight.Medium,
            color = FinoraColors.TextSecondary,
        )
    }
}
