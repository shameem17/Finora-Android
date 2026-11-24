package com.shameem.finora.screens.home.model

import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.ui.BreakdownItem
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.savings.model.SavingsAccount
import kotlin.math.max
import kotlin.math.min

/** The three tiles at the top of Home (iOS `SummaryType`). */
enum class SummaryType(val title: String, val brandColor: CustomColor, val icon: String) {
    Income("Income", CustomColor.MintGreen, "arrow.down.circle"),
    Expense("Expense", CustomColor.Accent, "arrow.up.circle"),
    Balance("Balance", CustomColor.Pest, "dollarsign.gauge.chart.leftthird.topthird.rightthird"),
}

/** This month's income and spending, and what they say about the rest of the month (iOS `MonthlyInsights`). */
data class MonthlyInsights(
    val income: Double,
    val expense: Double,
    val previousMonthExpense: Double,
    val elapsedDays: Int,
    val totalDays: Int,
    val topExpense: BreakdownItem,
) {
    sealed interface Pace {
        data class OnTrack(val surplus: Double) : Pace
        data class Overspending(val deficit: Double) : Pace
    }

    val balance: Double get() = income - expense

    /** Share of income not spent, clamped to 0…1. */
    val savingsRate: Double get() = if (income > 0) min(max(balance / income, 0.0), 1.0) else 0.0

    val dailyAverage: Double get() = if (elapsedDays > 0) expense / elapsedDays else 0.0
    val projectedExpense: Double get() = dailyAverage * totalDays

    val pace: Pace
        get() {
            val diff = income - projectedExpense
            return if (diff >= 0) Pace.OnTrack(diff) else Pace.Overspending(-diff)
        }

    /** Change in spending against last month; null when there is nothing to compare with. */
    val expenseChange: Double?
        get() = if (previousMonthExpense > 0) (expense - previousMonthExpense) / previousMonthExpense else null

    fun amount(type: SummaryType): Double = when (type) {
        SummaryType.Income -> income
        SummaryType.Expense -> expense
        SummaryType.Balance -> balance
    }

}

/** Everything the Net worth card shows (iOS `FinanceOverview`). */
data class FinanceOverview(
    val accountsTotal: Double = 0.0,
    val accountCount: Int = 0,
    val toCollect: Double = 0.0,
    val toRepay: Double = 0.0,
    val savings: Double = 0.0,
    val savingsCount: Int = 0,
    val monthBalance: Double = 0.0,
) {
    val netWorth: Double get() = accountsTotal + toCollect - toRepay

    companion object {
        val Empty = FinanceOverview()

        fun make(
            accounts: List<Account>,
            loans: List<Loan>,
            savings: List<SavingsAccount>,
            monthBalance: Double,
        ): FinanceOverview {
            var toCollect = 0.0
            var toRepay = 0.0
            for (loan in loans) {
                if (loan.isSettled) continue
                if (loan.kind == LoanKind.Lent) toCollect += loan.outstanding else toRepay += loan.outstanding
            }
            return FinanceOverview(
                accountsTotal = accounts.sumOf { it.balance },
                accountCount = accounts.size,
                toCollect = toCollect,
                toRepay = toRepay,
                savings = savings.sumOf { it.currentSavings },
                savingsCount = savings.size,
                monthBalance = monthBalance,
            )
        }
    }
}

data class HomeViewData(
    val headerTitle: String,
    val accounts: List<Account>,
    val insights: MonthlyInsights,
    val overview: FinanceOverview,
)
