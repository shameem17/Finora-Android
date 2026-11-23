package com.shameem.finora.screens.cashflow.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.cashflow.model.addTitle
import com.shameem.finora.screens.cashflow.service.CashFlowService
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.home.model.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/** Editable state of the add / edit income-or-expense form (iOS `CashFlowFormViewModel`). */
@Stable
class CashFlowFormViewModel(
    val mode: Mode,
    val accounts: List<Account>,
    val categories: List<TransactionCategory>,
    private val service: CashFlowService,
    private val scope: CoroutineScope,
) {
    sealed interface Mode {
        data class Create(val kind: CategoryKind) : Mode
        data class Edit(val entry: CashFlowEntry) : Mode
    }

    val kind: CategoryKind = when (mode) {
        is Mode.Create -> mode.kind
        is Mode.Edit -> mode.entry.kind
    }

    var amountText: String by mutableStateOf("")
    var categoryId: UUID? by mutableStateOf(null)
    var accountId: UUID? by mutableStateOf(null)
    var date: Instant by mutableStateOf(Instant.now())
    var note: String by mutableStateOf("")

    init {
        if (mode is Mode.Edit) {
            val entry = mode.entry
            amountText = DecimalInput.text(entry.amount)
            categoryId = entry.categoryId
            accountId = entry.accountId
            date = entry.date
            note = entry.note
        } else {
            categoryId = categories.firstOrNull()?.id
            accountId = accounts.firstOrNull()?.id
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = (if (isEditing) "Edit " else "New ") + kind.title
    val saveTitle: String get() = if (isEditing) "Save Changes" else kind.addTitle
    val hasCategories: Boolean get() = categories.isNotEmpty()

    private val editingEntry: CashFlowEntry? get() = (mode as? Mode.Edit)?.entry

    val amount: Double get() = DecimalInput.value(amountText)
    val account: Account? get() = accounts.firstOrNull { it.id == accountId }

    /** The chosen account's balance once this entry is applied (null until there's an amount and account). */
    val balanceAfter: Double?
        get() {
            val account = account ?: return null
            if (amount <= 0) return null
            var balance = account.balance
            // When editing within the same account, take the old effect back out first.
            editingEntry?.takeIf { it.accountId == account.id }?.let { balance -= it.accountDelta }
            return balance + if (kind == CategoryKind.Income) amount else -amount
        }

    val visibleError: String?
        get() {
            val after = balanceAfter ?: return null
            val account = account ?: return null
            if (kind != CategoryKind.Expense || after >= -0.005) return null
            return "${account.name} doesn't have enough balance."
        }

    val canSubmit: Boolean
        get() = amount > 0 && categoryId != null && account != null && visibleError == null

    /** Saves the entry. Returns true if valid (so the sheet may close). */
    fun submit(): Boolean {
        val categoryId = categoryId
        val accountId = accountId
        if (!canSubmit || categoryId == null || accountId == null) return false
        val entry = CashFlowEntry(
            id = editingEntry?.id ?: UUID.randomUUID(),
            kind = kind,
            categoryId = categoryId,
            accountId = accountId,
            amount = amount,
            date = date,
            note = note.trim(),
        )
        scope.launch { if (isEditing) service.update(entry) else service.record(entry) }
        return true
    }

    fun delete() {
        val id = editingEntry?.id ?: return
        scope.launch { service.delete(id) }
    }
}
