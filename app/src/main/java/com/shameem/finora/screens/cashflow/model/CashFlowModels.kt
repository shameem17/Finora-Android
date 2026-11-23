package com.shameem.finora.screens.cashflow.model

import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Instant
import java.util.UUID

/** One recorded income or expense (iOS `CashFlowEntry`). */
data class CashFlowEntry(
    val id: UUID = UUID.randomUUID(),
    val kind: CategoryKind,
    val categoryId: UUID,
    val accountId: UUID,
    val amount: Double,
    val date: Instant = Instant.now(),
    val note: String = "",
) {
    /** How this entry moves its account's balance. */
    val accountDelta: Double get() = if (kind == CategoryKind.Income) amount else -amount
}

/** An entry together with the category and account it refers to (either may since have been removed). */
data class CashFlowItem(
    val entry: CashFlowEntry,
    val category: TransactionCategory?,
    val account: AccountSummary?,
) {
    val id: UUID get() = entry.id
}

val CategoryKind.screenTitle: String get() = if (this == CategoryKind.Income) "Income" else "Expenses"
val CategoryKind.addTitle: String get() = if (this == CategoryKind.Income) "Add Income" else "Add Expense"
val CategoryKind.totalTitle: String get() = if (this == CategoryKind.Income) "Total Income" else "Total Expense"
val CategoryKind.centerCaption: String get() = if (this == CategoryKind.Income) "Earned" else "Spent"
val CategoryKind.accountTitle: String get() = if (this == CategoryKind.Income) "Deposit to" else "Pay from"
val CategoryKind.tint: CustomColor get() = CustomColor.Accent
