package com.shameem.finora.screens.cashflow.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.BreakdownItem
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.cashflow.model.CashFlowItem
import com.shameem.finora.screens.cashflow.service.CashFlowDependencies
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CashFlowUiState(
    val period: ReportPeriod = ReportPeriod.Month,
    val items: List<BreakdownItem> = emptyList(),
    val entries: List<CashFlowItem> = emptyList(),
    val total: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val categories: List<TransactionCategory> = emptyList(),
)

interface CashFlowViewModelProtocol {
    val kind: CategoryKind
    val state: StateFlow<CashFlowUiState>

    fun setPeriod(period: ReportPeriod)
    fun load()
    fun makeFormViewModel(editing: CashFlowEntry?): CashFlowFormViewModel
}

class CashFlowViewModel(
    override val kind: CategoryKind,
    private val dependencies: CashFlowDependencies,
) : ViewModel(), CashFlowViewModelProtocol {

    private val _state = MutableStateFlow(CashFlowUiState())
    override val state: StateFlow<CashFlowUiState> = _state.asStateFlow()

    private var rebuildJob: Job? = null

    init {
        viewModelScope.launch {
            merge(
                dependencies.entries.changes,
                dependencies.categories.changes,
                dependencies.accounts.changes,
            ).collect { load() }
        }
        load()
    }

    override fun setPeriod(period: ReportPeriod) {
        if (_state.value.period == period) return
        _state.update { it.copy(period = period) }
        load()
    }

    override fun load() {
        rebuildJob?.cancel()
        rebuildJob = viewModelScope.launch { rebuild() }
    }

    override fun makeFormViewModel(editing: CashFlowEntry?): CashFlowFormViewModel {
        val current = _state.value
        return CashFlowFormViewModel(
            mode = editing?.let { CashFlowFormViewModel.Mode.Edit(it) } ?: CashFlowFormViewModel.Mode.Create(kind),
            accounts = current.accounts,
            categories = current.categories,
            service = dependencies.service,
            scope = viewModelScope,
        )
    }

    private suspend fun rebuild() {
        val period = _state.value.period
        val categoryList = dependencies.categories.fetchCategories(kind)
        val accountList = dependencies.accounts.fetchAccounts()
        val categories = categoryList.associateBy { it.id }
        val accounts = accountList.associate { it.id to AccountSummary(it) }

        val inPeriod = dependencies.entries.fetchEntries(kind)
            .filter { period.contains(it.date) }
            .sortedByDescending { it.date }

        val items = inPeriod.groupBy { it.categoryId }.map { (id, group) ->
            val amount = group.sumOf { it.amount }
            categories[id]?.let {
                BreakdownItem(it.name, amount, colorFromHex(it.tint), it.icon)
            } ?: BreakdownItem("Uncategorised", amount, FinoraColors.TextTertiary, "questionmark.circle")
        }.sortedByDescending { it.amount }

        val sortedCategories = categoryList.sortedBy { it.name.lowercase() }

        _state.update {
            // Drop the result if the user switched period while this was loading.
            if (it.period != period) it else it.copy(
                items = items,
                entries = inPeriod.map { e -> CashFlowItem(e, categories[e.categoryId], accounts[e.accountId]) },
                total = inPeriod.sumOf { e -> e.amount },
                accounts = accountList,
                categories = sortedCategories,
            )
        }
    }
}
