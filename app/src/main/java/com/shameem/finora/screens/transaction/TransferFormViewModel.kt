package com.shameem.finora.screens.transaction

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import java.time.Instant
import java.util.UUID

sealed interface TransferValidationError {
    data object MissingSource : TransferValidationError
    data object MissingDestination : TransferValidationError
    data object SameAccount : TransferValidationError
    data object InvalidAmount : TransferValidationError
    data class InsufficientBalance(val available: Double) : TransferValidationError

    /** User-facing text; null for the "form isn't complete yet" cases that show no banner. */
    val message: String?
        get() = when (this) {
            SameAccount -> "Choose two different accounts."
            is InsufficientBalance -> "Not enough balance. Available: ${CurrencyFormatter.taka(available, showFraction = true)}"
            MissingSource, MissingDestination, InvalidAmount -> null
        }
}

/** Editable state of the "New Transfer" form (iOS `TransferFormViewModel`). */
@Stable
class TransferFormViewModel(
    val accounts: List<Account>,
    private val onSave: (Transfer) -> Unit,
) {
    val id: UUID = UUID.randomUUID()

    var fromId: UUID? by mutableStateOf(accounts.firstOrNull()?.id)
    var toId: UUID? by mutableStateOf(null)
    var amountText: String by mutableStateOf("")
    var chargeText: String by mutableStateOf("")
    var date: Instant by mutableStateOf(Instant.now())
    var reason: String by mutableStateOf("")

    val fromAccount: Account? get() = accounts.firstOrNull { it.id == fromId }
    val toAccount: Account? get() = accounts.firstOrNull { it.id == toId }

    val amount: Double get() = DecimalInput.value(amountText)
    val charge: Double get() = DecimalInput.value(chargeText)
    val totalDebit: Double get() = amount + charge

    /** Source balance after this transfer; null until an amount is entered. */
    val balanceAfter: Double?
        get() {
            val from = fromAccount ?: return null
            if (amount <= 0) return null
            return from.balance - totalDebit
        }

    val visibleError: String? get() = validate()?.message
    val canSubmit: Boolean get() = validate() == null

    fun swapAccounts() {
        val previousFrom = fromId
        fromId = toId
        toId = previousFrom
    }

    /** Builds and emits the transfer. Returns true if the form was valid (so the sheet may close). */
    fun submit(): Boolean {
        if (validate() != null) return false
        val from = fromAccount ?: return false
        val to = toAccount ?: return false
        onSave(
            Transfer(
                id = UUID.randomUUID(),
                from = AccountSummary(from),
                to = AccountSummary(to),
                amount = amount,
                charge = charge,
                date = date,
                reason = reason.trim(),
            ),
        )
        return true
    }

    fun validate(): TransferValidationError? {
        val from = fromAccount ?: return TransferValidationError.MissingSource
        val to = toAccount ?: return TransferValidationError.MissingDestination
        if (from.id == to.id) return TransferValidationError.SameAccount
        if (amount <= 0) return TransferValidationError.InvalidAmount
        if (totalDebit > from.balance + 0.005) return TransferValidationError.InsufficientBalance(from.balance)
        return null
    }
}
