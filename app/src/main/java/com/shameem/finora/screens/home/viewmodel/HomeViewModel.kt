package com.shameem.finora.screens.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.BreakdownItem
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.cashflow.repository.CashFlowRepository
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.repository.CategoryRepository
import com.shameem.finora.screens.home.model.FinanceOverview
import com.shameem.finora.screens.home.model.HomeViewData
import com.shameem.finora.screens.home.model.MonthlyInsights
import com.shameem.finora.screens.loan.repository.LoanRepository
import com.shameem.finora.screens.savings.repository.SavingsRepository
import com.shameem.finora.screens.transaction.AccountRepository
import com.shameem.finora.services.date.DateService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.YearMonth

interface HomeViewModelProtocol {
    /** Null until the first load finishes. */
    val homeViewData: StateFlow<HomeViewData?>

    fun prepareData()
}

class HomeViewModel(
    private val dateService: DateService,
    private val accountRepository: AccountRepository,
    private val cashFlowRepository: CashFlowRepository,
    private val categoryRepository: CategoryRepository,
    private val loanRepository: LoanRepository,
    private val savingsRepository: SavingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel(), HomeViewModelProtocol {

    private val _homeViewData = MutableStateFlow<HomeViewData?>(null)
    override val homeViewData: StateFlow<HomeViewData?> = _homeViewData.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            merge(
                accountRepository.changes,
                cashFlowRepository.changes,
                categoryRepository.changes,
                loanRepository.changes,
                savingsRepository.changes,
            ).collect { if (_homeViewData.value != null) prepareData() }
        }
    }

    override fun prepareData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val date = dateService.currentDate()
            val accounts = accountRepository.fetchAccounts()
            val insights = makeInsights()
            _homeViewData.value = HomeViewData(
                headerTitle = "${date.month} ${date.year}, Overview",
                accounts = accounts,
                insights = insights,
                overview = FinanceOverview.make(
                    accounts = accounts,
                    loans = loanRepository.fetchLoans(),
                    savings = savingsRepository.fetchSavings(),
                    monthBalance = insights.balance,
                ),
            )
        }
    }

    private suspend fun makeInsights(): MonthlyInsights {
        val zone = clock.zone
        val current = YearMonth.now(clock)
        val previous = current.minusMonths(1)
        fun CashFlowEntry.month(): YearMonth = YearMonth.from(date.atZone(zone))

        val income = cashFlowRepository.fetchEntries(CategoryKind.Income)
            .filter { it.month() == current }
            .sumOf { it.amount }

        val expenses = cashFlowRepository.fetchEntries(CategoryKind.Expense)
        val monthExpenses = expenses.filter { it.month() == current }
        val expense = monthExpenses.sumOf { it.amount }
        val previousExpense = expenses.filter { it.month() == previous }.sumOf { it.amount }

        val categories = categoryRepository.fetchCategories(CategoryKind.Expense).associateBy { it.id }
        val top = monthExpenses
            .groupBy { it.categoryId }
            .map { (id, group) ->
                val amount = group.sumOf { it.amount }
                val category = categories[id]
                if (category != null) {
                    BreakdownItem(category.name, amount, colorFromHex(category.tint), category.icon)
                } else {
                    BreakdownItem("Uncategorised", amount, FinoraColors.TextTertiary, "questionmark.circle")
                }
            }
            .maxByOrNull { it.amount }
            ?: BreakdownItem("None yet", 0.0, FinoraColors.TextTertiary, "minus")

        val progress = dateService.monthProgress()
        return MonthlyInsights(
            income = income,
            expense = expense,
            previousMonthExpense = previousExpense,
            elapsedDays = progress.elapsedDays,
            totalDays = progress.totalDays,
            topExpense = top,
        )
    }
}
