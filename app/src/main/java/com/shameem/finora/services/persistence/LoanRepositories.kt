package com.shameem.finora.services.persistence

import androidx.room.withTransaction
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.repository.LoanRepository
import com.shameem.finora.screens.loan.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import java.text.Collator
import java.util.UUID

class RoomPersonRepository(database: FinoraDatabase) : PersonRepository {

    private val dao = database.personDao()
    private val signal = ChangeSignal()
    private val collator = Collator.getInstance().apply { strength = Collator.SECONDARY }

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchPeople(): List<Person> =
        dao.all().map(PersonEntity::toDomain).sortedWith { a, b -> collator.compare(a.name, b.name) }

    override suspend fun person(id: UUID): Person? = dao.byId(id.toString())?.toDomain()

    override suspend fun add(person: Person) {
        dao.insert(PersonEntity.from(person))
        signal.notifyChanged()
    }

    override suspend fun update(person: Person) {
        dao.updateDetails(person.id.toString(), person.name, person.category.storageKey, person.phone, person.address)
        signal.notifyChanged()
    }

    override suspend fun delete(id: UUID) {
        dao.delete(id.toString())
        signal.notifyChanged()
    }
}

class RoomLoanRepository(private val database: FinoraDatabase) : LoanRepository {

    private val dao = database.loanDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchLoans(): List<Loan> {
        val repaymentsByLoan = dao.allRepayments().groupBy({ it.loanId }, { it.toDomain() })
        return dao.all().map { it.toDomain(repaymentsByLoan[it.id].orEmpty()) }
    }

    override suspend fun loan(id: UUID): Loan? {
        val stored = dao.byId(id.toString()) ?: return null
        return stored.toDomain(dao.repaymentsOf(stored.id).map(RepaymentEntity::toDomain))
    }

    override suspend fun add(loan: Loan) {
        database.withTransaction {
            dao.insert(LoanEntity.from(loan))
            writeRepayments(loan)
        }
        signal.notifyChanged()
    }

    override suspend fun update(loan: Loan) {
        database.withTransaction {
            dao.updateDetails(
                id = loan.id.toString(),
                personId = loan.personId.toString(),
                interest = loan.interest,
                date = loan.date.toEpochMilli(),
                dueDate = loan.dueDate?.toEpochMilli(),
                installmentCount = loan.installmentCount,
                note = loan.note,
            )
            writeRepayments(loan)
        }
        signal.notifyChanged()
    }

    override suspend fun delete(id: UUID) {
        dao.delete(id.toString()) // repayments cascade
        signal.notifyChanged()
    }

    private suspend fun writeRepayments(loan: Loan) {
        dao.deleteRepaymentsOf(loan.id.toString())
        dao.upsertRepayments(loan.repayments.mapIndexed { i, r -> RepaymentEntity.from(r, loan.id, i) })
    }
}
