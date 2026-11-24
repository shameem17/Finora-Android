package com.shameem.finora.screens

import com.shameem.finora.core.ui.BreakdownItem
import com.shameem.finora.screens.home.model.FinanceOverview
import com.shameem.finora.screens.home.model.MonthlyInsights
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.transaction.AccountSummary
import com.shameem.finora.core.designsystem.FinoraColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.UUID

class DomainLogicTest {

    private val account = AccountSummary(UUID.randomUUID(), "Bank", "#013230", com.shameem.finora.screens.home.view.AccountType.Bank)

    private fun loan(kind: LoanKind = LoanKind.Lent, principal: Double = 1000.0, interest: Double = 0.0, count: Int = 1) =
        Loan(
            kind = kind, personId = UUID.randomUUID(), account = account,
            principal = principal, interest = interest,
            date = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
            dueDate = ZonedDateTime.of(2026, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
            installmentCount = count,
        )

    @Test
    fun installmentsSumToTotalAndLastAbsorbsRounding() {
        val installments = loan(principal = 1000.0, count = 3).installments(ZoneOffset.UTC)
        assertEquals(3, installments.size)
        assertEquals(1000.0, installments.sumOf { it.amount }, 0.001)
        assertEquals(333.33, installments[0].amount, 0.001)
        assertEquals(333.34, installments[2].amount, 0.001)
    }

    @Test
    fun outstandingIncludesInterest() {
        val l = loan(principal = 1000.0, interest = 100.0)
        assertEquals(1100.0, l.totalPayable, 0.001)
        assertEquals(1100.0, l.outstanding, 0.001)
        assertTrue(!l.isSettled)
    }

    @Test
    fun loanKindMovesBalanceInTheRightDirection() {
        assertEquals(-50.0, LoanKind.Lent.principalDelta(50.0), 0.0)
        assertEquals(50.0, LoanKind.Borrowed.principalDelta(50.0), 0.0)
        assertEquals(50.0, LoanKind.Lent.repaymentDelta(50.0), 0.0)
        assertEquals(-50.0, LoanKind.Borrowed.repaymentDelta(50.0), 0.0)
    }

    @Test
    fun netWorthIsAccountsPlusCollectMinusRepay() {
        val overview = FinanceOverview(accountsTotal = 1000.0, toCollect = 200.0, toRepay = 50.0)
        assertEquals(1150.0, overview.netWorth, 0.001)
    }

    @Test
    fun insightsSavingsRateIsClampedAndPaceProjects() {
        val top = BreakdownItem("Food", 100.0, FinoraColors.TextTertiary, "fork.knife")
        val overspending = MonthlyInsights(income = 1000.0, expense = 600.0, previousMonthExpense = 0.0, elapsedDays = 10, totalDays = 30, topExpense = top)
        assertEquals(0.4, overspending.savingsRate, 0.001)
        assertTrue(overspending.pace is MonthlyInsights.Pace.Overspending) // 60/day * 30 = 1800 > 1000
        assertNull(overspending.expenseChange)

        val broke = overspending.copy(income = 0.0)
        assertEquals(0.0, broke.savingsRate, 0.0)

        val saver = overspending.copy(expense = 100.0, previousMonthExpense = 200.0)
        assertTrue(saver.pace is MonthlyInsights.Pace.OnTrack)
        assertEquals(-0.5, saver.expenseChange!!, 0.001)
    }
}
