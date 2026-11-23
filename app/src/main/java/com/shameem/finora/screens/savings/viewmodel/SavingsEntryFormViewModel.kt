package com.shameem.finora.screens.savings.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.service.SavingsService
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import kotlin.math.max

/** Editable state of the deposit / withdrawal form (iOS `SavingsEntryFormViewModel`). */
@Stable
class SavingsEntryFormViewModel(
    val mode: Mode,
    val savings: SavingsAccount,
    val accounts: List<Account>,
    private val service: SavingsService,
    private val scope: CoroutineScope,
) {
    sealed interface Mode {
        data class Create(val kind: SavingsEntryKind) : Mode
        data class Edit(val entry: SavingsEntry) : Mode
    }

    val kind: SavingsEntryKind = when (mode) {
        is Mode.Create -> mode.kind
        is Mode.Edit -> mode.entry.kind
    }

    /** Whether the entry also moves money in one of the user's accounts. */
    var usesAccount: Boolean by mutableStateOf(false)
    var accountId: UUID? by mutableStateOf(null)
    var amountText: String by mutableStateOf("")
    var date: Instant by mutableStateOf(Instant.now())
    var note: String by mutableStateOf("")

    init {
        val fallback = if (accounts.any { it.id == savings.accountId }) savings.accountId else accounts.firstOrNull()?.id
        when (mode) {
            is Mode.Create -> {
                usesAccount = fallback != null
                accountId = fallback
            }
            is Mode.Edit -> {
                val entry = mode.entry
                usesAccount = entry.account != null
                accountId = entry.account?.id ?: fallback
                amountText = DecimalInput.text(entry.amount)
                date = entry.date
                note = entry.note
            }
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = if (isEditing) "Edit ${kind.historyTitle}" else kind.title
    val saveTitle: String get() = if (isEditing) "Save Changes" else kind.title

    private val editingEntry: SavingsEntry? get() = (mode as? Mode.Edit)?.entry

    val amount: Double get() = DecimalInput.value(amountText)
    val account: Account? get() = if (usesAccount) accounts.firstOrNull { it.id == accountId } else null

    /** What the pot holds with the entry being edited taken back out. */
    private val baseSavings: Double
        get() {
            val net = savings.totalDeposited - savings.totalWithdrawn
            val old = editingEntry ?: return net
            return net - if (old.kind == SavingsEntryKind.Deposit) old.amount else -old.amount
        }

    /** Currently saved amount, shown in the header and on the "Break all" chip. */
    val current: Double get() = savings.currentSavings

    /** Entries can't pre-date the pot. */
    val earliestDate: Instant
        get() = savings.startDate.atZone(ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault()).toInstant()

    val savingsAfter: Double?
        get() = if (amount > 0) (if (kind == SavingsEntryKind.Deposit) baseSavings + amount else baseSavings - amount) else null

    val balanceAfter: Double?
        get() {
            val account = account ?: return null
            if (amount <= 0) return null
            var balance = account.balance
            val old = editingEntry
            if (old?.account?.id == account.id) balance -= old.accountDelta // undo the old effect first
            return balance + if (kind == SavingsEntryKind.Deposit) -amount else amount
        }

    val visibleError: String?
        get() {
            if (amount <= 0) return null
            savingsAfter?.takeIf { it < -0.005 }?.let {
                return if (kind == SavingsEntryKind.Withdrawal) {
                    "That's more than you have saved: ${CurrencyFormatter.taka(max(baseSavings, 0.0), showFraction = true)}"
                } else {
                    "This would leave more withdrawn than saved."
                }
            }
            val after = balanceAfter
            val account = account
            if (kind == SavingsEntryKind.Deposit && after != null && after < -0.005 && account != null) {
                return "${account.name} doesn't have enough balance."
            }
            return null
        }

    val canSubmit: Boolean get() = amount > 0 && (!usesAccount || account != null) && visibleError == null

    fun withdrawAll() {
        amountText = DecimalInput.text(max(baseSavings, 0.0))
    }

    /**
     * Saves the entry. Returns true if valid (so the sheet may close). The checks above mirror the service's,
     * so the asynchronous save does not fail for a form that passed validation.
     */
    fun submit(): Boolean {
        if (!canSubmit) return false
        val entry = SavingsEntry(
            id = editingEntry?.id ?: UUID.randomUUID(),
            kind = kind,
            amount = amount,
            date = date,
            note = note.trim(),
            account = account?.let(::AccountSummary),
        )
        scope.launch {
            when (mode) {
                is Mode.Create -> when (kind) {
                    SavingsEntryKind.Deposit -> service.deposit(entry, savings.id)
                    SavingsEntryKind.Withdrawal -> service.withdraw(entry, savings.id)
                }
                is Mode.Edit -> service.updateEntry(entry, savings.id)
            }
        }
        return true
    }
}
