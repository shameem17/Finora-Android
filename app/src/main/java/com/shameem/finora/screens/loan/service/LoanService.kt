package com.shameem.finora.screens.loan.service

import com.shameem.finora.core.data.ImmediateUnitOfWork
import com.shameem.finora.core.data.UnitOfWork
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.Repayment
import com.shameem.finora.screens.loan.repository.LoanRepository
import com.shameem.finora.screens.loan.repository.PersonRepository
import com.shameem.finora.screens.transaction.AccountRepository
import java.util.UUID

/** Loan operations that also move money in the linked accounts, atomically (iOS `LoanService`). */
interface LoanService {
    suspend fun create(loan: Loan)
    suspend fun update(loan: Loan)
    suspend fun delete(loanId: UUID)
    suspend fun addRepayment(repayment: Repayment, loanId: UUID)
    suspend fun deleteRepayment(id: UUID, loanId: UUID)
}

class DefaultLoanService(
    private val loans: LoanRepository,
    private val people: PersonRepository,
    private val accounts: AccountRepository,
    private val reminders: LoanReminderScheduling,
    private val unitOfWork: UnitOfWork = ImmediateUnitOfWork,
) : LoanService {

    override suspend fun create(loan: Loan) {
        unitOfWork.perform {
            accounts.adjustBalance(loan.account.id, loan.kind.principalDelta(loan.principal))
            loans.add(loan)
        }
        syncReminder(loan.id)
    }

    override suspend fun update(loan: Loan) {
        unitOfWork.perform { loans.update(loan) }
        syncReminder(loan.id)
    }

    override suspend fun delete(loanId: UUID) {
        unitOfWork.perform {
            val loan = loans.loan(loanId) ?: return@perform
            // Undo everything the loan did to account balances.
            accounts.adjustBalance(loan.account.id, -loan.kind.principalDelta(loan.principal))
            for (repayment in loan.repayments) {
                accounts.adjustBalance(repayment.account.id, -loan.kind.repaymentDelta(repayment.amount))
            }
            loans.delete(loanId)
        }
        reminders.cancel(loanId)
    }

    override suspend fun addRepayment(repayment: Repayment, loanId: UUID) {
        unitOfWork.perform {
            val loan = loans.loan(loanId) ?: return@perform
            accounts.adjustBalance(repayment.account.id, loan.kind.repaymentDelta(repayment.amount))
            loans.update(loan.copy(repayments = loan.repayments + repayment))
        }
        syncReminder(loanId)
    }

    override suspend fun deleteRepayment(id: UUID, loanId: UUID) {
        unitOfWork.perform {
            val loan = loans.loan(loanId) ?: return@perform
            val repayment = loan.repayments.firstOrNull { it.id == id } ?: return@perform
            accounts.adjustBalance(repayment.account.id, -loan.kind.repaymentDelta(repayment.amount))
            loans.update(loan.copy(repayments = loan.repayments.filterNot { it.id == id }))
        }
        syncReminder(loanId)
    }

    private suspend fun syncReminder(id: UUID) {
        val loan = loans.loan(id)
        if (loan == null) reminders.cancel(id) else reminders.sync(loan, people.person(loan.personId))
    }
}

data class LoanDependencies(
    val loans: LoanRepository,
    val people: PersonRepository,
    val accounts: AccountRepository,
    val service: LoanService,
)
