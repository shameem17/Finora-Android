package com.shameem.finora.screens.loan.model

import com.shameem.finora.core.util.ReportPeriod
import java.time.Clock
import java.time.LocalDate

/** Totals shown on the loan overview card for the selected period (iOS `LoanSummary`). */
data class LoanSummary(
    val toReceive: Double = 0.0,
    val toPay: Double = 0.0,
    val activeLentCount: Int = 0,
    val activeBorrowedCount: Int = 0,
    val lentInPeriod: Double = 0.0,
    val borrowedInPeriod: Double = 0.0,
    val receivedInPeriod: Double = 0.0,
    val repaidInPeriod: Double = 0.0,
    val dueToReceive: Double = 0.0,
    val dueToPay: Double = 0.0,
    val overdueToReceive: Double = 0.0,
    val overdueToPay: Double = 0.0,
) {
    val net: Double get() = toReceive - toPay

    companion object {
        val Empty = LoanSummary()

        fun make(loans: List<Loan>, period: ReportPeriod, clock: Clock = Clock.systemDefaultZone()): LoanSummary {
            val zone = clock.zone
            val now = LocalDate.now(clock)
            val periodEnd = period.endDate(zone, now) // null for "All": nothing is cut off
            val todayStart = now.atStartOfDay(zone).toInstant()

            var toReceive = 0.0; var toPay = 0.0
            var activeLent = 0; var activeBorrowed = 0
            var lentInPeriod = 0.0; var borrowedInPeriod = 0.0
            var receivedInPeriod = 0.0; var repaidInPeriod = 0.0
            var dueToReceive = 0.0; var dueToPay = 0.0
            var overdueToReceive = 0.0; var overdueToPay = 0.0

            for (loan in loans) {
                val isLent = loan.kind == LoanKind.Lent
                if (period.contains(loan.date, zone, now)) {
                    if (isLent) lentInPeriod += loan.principal else borrowedInPeriod += loan.principal
                }
                val repaid = loan.repayments.filter { period.contains(it.date, zone, now) }.sumOf { it.amount }
                if (isLent) receivedInPeriod += repaid else repaidInPeriod += repaid

                if (loan.isSettled) continue
                val due = loan.amountDue(periodEnd ?: java.time.Instant.MAX, zone)
                val overdue = loan.amountDue(todayStart, zone)
                if (isLent) {
                    toReceive += loan.outstanding; activeLent++
                    dueToReceive += due; overdueToReceive += overdue
                } else {
                    toPay += loan.outstanding; activeBorrowed++
                    dueToPay += due; overdueToPay += overdue
                }
            }

            return LoanSummary(
                toReceive, toPay, activeLent, activeBorrowed, lentInPeriod, borrowedInPeriod,
                receivedInPeriod, repaidInPeriod, dueToReceive, dueToPay, overdueToReceive, overdueToPay,
            )
        }
    }
}

/** A titled group of loans in the list (Overdue / Active / Settled). */
data class LoanSection(
    val group: Group,
    val items: List<LoanItem>,
) {
    enum class Group(val title: String) {
        Overdue("Overdue"),
        Active("Active"),
        Settled("Settled"),
    }

    val id: Group get() = group
}

