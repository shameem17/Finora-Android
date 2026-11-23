package com.shameem.finora.screens.loan.repository

import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.Person
import java.util.UUID

interface LoanRepository : ChangeNotifying {
    suspend fun fetchLoans(): List<Loan>
    suspend fun loan(id: UUID): Loan?
    suspend fun add(loan: Loan)

    /** Saves the editable fields and makes the stored repayments match [loan].repayments. */
    suspend fun update(loan: Loan)

    /** Deletes the loan and its repayments. */
    suspend fun delete(id: UUID)
}

interface PersonRepository : ChangeNotifying {
    /** Alphabetical, case-insensitive. */
    suspend fun fetchPeople(): List<Person>
    suspend fun person(id: UUID): Person?
    suspend fun add(person: Person)
    suspend fun update(person: Person)
    suspend fun delete(id: UUID)
}
