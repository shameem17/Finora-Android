package com.shameem.finora.screens.loan.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.service.LoanDependencies
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

sealed interface LoanFormError {
    data object MissingPerson : LoanFormError
    data object MissingAccount : LoanFormError
    data object InvalidAmount : LoanFormError
    data class InsufficientBalance(val available: Double) : LoanFormError
    data object DueBeforeLoanDate : LoanFormError
    data class BelowRepaid(val repaid: Double) : LoanFormError

    val message: String?
        get() = when (this) {
            is InsufficientBalance -> "Not enough balance. Available: ${CurrencyFormatter.taka(available, showFraction = true)}"
            DueBeforeLoanDate -> "The due date can't be before the loan date."
            is BelowRepaid -> "Total can't be less than the ${CurrencyFormatter.taka(repaid, showFraction = true)} already repaid."
            MissingPerson, MissingAccount, InvalidAmount -> null
        }
}

/** Editable state of the add / edit loan form (iOS `LoanFormViewModel`). */
@Stable
class LoanFormViewModel(
    val mode: Mode,
    /** Accounts to pick from when creating; empty when editing (the account is locked). */
    val accounts: List<Account>,
    people: List<Person>,
    private val dependencies: LoanDependencies,
    private val scope: CoroutineScope,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    sealed interface Mode {
        data class Create(val kind: LoanKind) : Mode
        data class Edit(val loan: Loan) : Mode
    }

    var people: List<Person> by mutableStateOf(people)
        private set

    var kind: LoanKind by mutableStateOf(LoanKind.Lent)
    var personId: UUID? by mutableStateOf(null)
    var accountId: UUID? by mutableStateOf(null)
    var amountText: String by mutableStateOf("")
    var interestText: String by mutableStateOf("")
    var date: Instant by mutableStateOf(Instant.now())
    var hasDueDate: Boolean by mutableStateOf(false)
    var dueDate: Instant by mutableStateOf(ZonedDateTime.now(zone).plusMonths(1).toInstant())
    var installmentCount: Int by mutableStateOf(1)
    var note: String by mutableStateOf("")

    private val editingLoan: Loan? = (mode as? Mode.Edit)?.loan

    init {
        when (mode) {
            is Mode.Create -> {
                kind = mode.kind
                accountId = accounts.firstOrNull()?.id
            }
            is Mode.Edit -> {
                val loan = mode.loan
                kind = loan.kind
                accountId = loan.account.id
                personId = loan.personId
                amountText = DecimalInput.text(loan.principal)
                interestText = if (loan.interest > 0) DecimalInput.text(loan.interest) else ""
                date = loan.date
                loan.dueDate?.let {
                    hasDueDate = true
                    dueDate = it
                }
                installmentCount = loan.installmentCount
                note = loan.note ?: ""
            }
        }
    }

    val isEditing: Boolean get() = editingLoan != null

    val title: String
        get() = when {
            !isEditing && kind == LoanKind.Lent -> "New Loan"
            !isEditing -> "New Borrow"
            kind == LoanKind.Lent -> "Edit Loan"
            else -> "Edit Borrow"
        }

    val saveTitle: String
        get() = if (isEditing) "Save Changes" else if (kind == LoanKind.Lent) "Save Loan" else "Save Borrow"

    val person: Person? get() = people.firstOrNull { it.id == personId }
    val account: Account? get() = accounts.firstOrNull { it.id == accountId }
    val lockedAccount: AccountSummary? get() = editingLoan?.account

    /** The principal; fixed once the loan exists, because it has already moved money. */
    val amount: Double get() = editingLoan?.principal ?: DecimalInput.value(amountText)
    val interest: Double get() = DecimalInput.value(interestText)
    val totalPayable: Double get() = amount + interest

    val installmentAmount: Double?
        get() = if (hasDueDate && installmentCount > 1) totalPayable / installmentCount else null

    val balanceAfter: Double?
        get() {
            val account = account
            if (isEditing || account == null || amount <= 0) return null
            return account.balance + kind.principalDelta(amount)
        }

    val validationError: LoanFormError? get() = validate()
    val visibleError: String? get() = validationError?.message
    val canSubmit: Boolean get() = validationError == null

    /** Keeps the due date from sitting before a newly chosen loan date. */
    fun onLoanDateChanged(newDate: Instant) {
        date = newDate
        if (day(dueDate) < day(newDate)) dueDate = newDate
    }

    /** A form for adding a person from here; the new person is selected once saved. */
    suspend fun makePersonFormViewModel(): PersonFormViewModel =
        PersonFormViewModel(
            mode = PersonFormViewModel.Mode.Create,
            repository = dependencies.people,
            scope = scope,
            existingPeople = dependencies.people.fetchPeople(),
            onSaved = { saved ->
                scope.launch {
                    people = dependencies.people.fetchPeople()
                    personId = saved.id
                }
            },
        )

    /** Saves the loan. Returns true if valid (so the sheet may close). */
    fun submit(): Boolean {
        val personId = personId
        if (validate() != null || personId == null) return false

        val due = if (hasDueDate) day(dueDate).atStartOfDay(zone).toInstant() else null
        val installments = if (due == null) 1 else installmentCount.coerceIn(1, Loan.MAX_INSTALLMENTS)
        val finalNote = note.trim().ifEmpty { null }

        val existing = editingLoan
        if (existing != null) {
            scope.launch {
                // Re-read so repayments added since the form opened are kept.
                val stored = dependencies.loans.loan(existing.id) ?: existing
                dependencies.service.update(
                    stored.copy(
                        personId = personId,
                        interest = interest,
                        date = date,
                        dueDate = due,
                        installmentCount = installments,
                        note = finalNote,
                    ),
                )
            }
        } else {
            val account = account ?: return false
            val loan = Loan(
                kind = kind,
                personId = personId,
                account = AccountSummary(account),
                principal = amount,
                interest = interest,
                date = date,
                dueDate = due,
                installmentCount = installments,
                note = finalNote,
            )
            scope.launch { dependencies.service.create(loan) }
        }
        return true
    }

    private fun validate(): LoanFormError? {
        if (person == null) return LoanFormError.MissingPerson
        val existing = editingLoan
        if (existing != null) {
            if (existing.totalRepaid > totalPayable + Loan.TOLERANCE) return LoanFormError.BelowRepaid(existing.totalRepaid)
        } else {
            val account = account ?: return LoanFormError.MissingAccount
            if (amount <= 0) return LoanFormError.InvalidAmount
            if (kind == LoanKind.Lent && amount > account.balance + Loan.TOLERANCE) {
                return LoanFormError.InsufficientBalance(account.balance)
            }
        }
        if (hasDueDate && day(dueDate) < day(date)) return LoanFormError.DueBeforeLoanDate
        return null
    }

    private fun day(instant: Instant): LocalDate = instant.atZone(zone).toLocalDate()
}
