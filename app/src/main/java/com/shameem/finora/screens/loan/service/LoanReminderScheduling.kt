package com.shameem.finora.screens.loan.service

import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.Person
import java.util.UUID

/** Schedules the local "payment due" reminder for a loan's next instalment. */
interface LoanReminderScheduling {
    /** Replaces any existing reminder for [loan] with one for its next unpaid instalment (or none). */
    fun sync(loan: Loan, person: Person?)

    fun cancel(loanId: UUID)
}

object NoopLoanReminderScheduler : LoanReminderScheduling {
    override fun sync(loan: Loan, person: Person?) = Unit
    override fun cancel(loanId: UUID) = Unit
}
