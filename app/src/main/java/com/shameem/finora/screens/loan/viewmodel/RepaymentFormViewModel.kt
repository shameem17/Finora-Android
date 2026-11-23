package com.shameem.finora.screens.loan.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.loan.model.Installment
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanItem
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Repayment
import com.shameem.finora.screens.loan.service.LoanService
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.math.max

sealed interface RepaymentValidationError {
    data object MissingAccount : RepaymentValidationError
    data object InvalidAmount : RepaymentValidationError
    data class ExceedsOutstanding(val outstanding: Double) : RepaymentValidationError
    data class InsufficientBalance(val available: Double) : RepaymentValidationError

    val message: String?
        get() = when (this) {
            is ExceedsOutstanding -> "That's more than what's left: ${CurrencyFormatter.taka(outstanding, showFraction = true)}"
            is InsufficientBalance -> "Not enough balance. Available: ${CurrencyFormatter.taka(available, showFraction = true)}"
            MissingAccount, InvalidAmount -> null
        }
}

/** Editable state of the "record a repayment" form (iOS `RepaymentFormViewModel`). */
@Stable
class RepaymentFormViewModel(
    val item: LoanItem,
    val accounts: List<Account>,
    private val service: LoanService,
    private val scope: CoroutineScope,
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    var accountId: UUID? by mutableStateOf(
        (accounts.firstOrNull { it.id == item.loan.account.id } ?: accounts.firstOrNull())?.id,
    )
    var amountText: String by mutableStateOf("")
    var date: Instant by mutableStateOf(Instant.now())
    var note: String by mutableStateOf("")

    val loan: Loan get() = item.loan
    val kind: LoanKind get() = loan.kind
    val outstanding: Double get() = loan.outstanding
    val account: Account? get() = accounts.firstOrNull { it.id == accountId }
    val amount: Double get() = DecimalInput.value(amountText)

    /** The next unpaid instalment, only for loans that are actually split into several. */
    val nextInstallment: Installment?
        get() = loan.nextInstallment(zone)?.takeIf { loan.installmentCount > 1 }

    val earliestDate: Instant get() = loan.date.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()

    val remainingAfter: Double? get() = if (amount > 0) max(outstanding - amount, 0.0) else null

    val balanceAfter: Double?
        get() {
            val account = account ?: return null
            return if (amount > 0) account.balance + kind.repaymentDelta(amount) else null
        }

    val validationError: RepaymentValidationError? get() = validate()
    val visibleError: String? get() = validationError?.message
    val canSubmit: Boolean get() = validationError == null

    fun payInFull() {
        amountText = DecimalInput.text(outstanding)
    }

    fun payNextInstallment() {
        val next = nextInstallment ?: return
        amountText = DecimalInput.text(next.remaining)
    }

    /** Records the repayment. Returns true if valid (so the sheet may close). */
    fun submit(): Boolean {
        val account = account
        if (validate() != null || account == null) return false
        val repayment = Repayment(
            amount = amount,
            date = date,
            account = AccountSummary(account),
            note = note.trim().ifEmpty { null },
        )
        scope.launch { service.addRepayment(repayment, loan.id) }
        return true
    }

    private fun validate(): RepaymentValidationError? {
        val account = account ?: return RepaymentValidationError.MissingAccount
        if (amount <= 0) return RepaymentValidationError.InvalidAmount
        if (amount > outstanding + Loan.TOLERANCE) return RepaymentValidationError.ExceedsOutstanding(outstanding)
        if (kind == LoanKind.Borrowed && amount > account.balance + Loan.TOLERANCE) {
            return RepaymentValidationError.InsufficientBalance(account.balance)
        }
        return null
    }
}
