package com.shameem.finora.screens.cashflow.coordinator

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.cashflow.view.CashFlowView
import com.shameem.finora.screens.cashflow.viewmodel.CashFlowViewModel
import com.shameem.finora.screens.categories.model.CategoryKind

/**
 * Coordinator for the Income and Expense tabs (iOS `IncomeCoordinator` / `ExpenseCoordinator`, which differ
 * only by [kind]).
 */
class CashFlowCoordinator(
    private val kind: CategoryKind,
    private val dependencies: AppDependencies,
) : Coordinator {

    @Composable
    override fun Start() {
        val viewModel = rememberViewModel(key = kind.name) { CashFlowViewModel(kind, dependencies.cashFlow) }
        CashFlowView(model = viewModel)
    }
}
