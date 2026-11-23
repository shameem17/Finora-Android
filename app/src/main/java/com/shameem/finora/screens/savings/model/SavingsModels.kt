package com.shameem.finora.screens.savings.model

import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.ui.PickerOption
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountType
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

enum class SavingsEntryKind(val storageKey: String) {
    Deposit("deposit"),
    Withdrawal("withdrawal");

    val title: String get() = if (this == Deposit) "Add Savings" else "Withdraw"
    val historyTitle: String get() = if (this == Deposit) "Deposit" else "Withdrawal"
    val icon: String get() = if (this == Deposit) "arrow.down.to.line" else "arrow.up.right"
    val tint: CustomColor get() = CustomColor.Accent
    val accountTitle: String get() = if (this == Deposit) "Take money from" else "Send money to"
    val sign: String get() = if (this == Deposit) "+" else "−"

    companion object {
        fun fromStorageKey(key: String): SavingsEntryKind? = entries.firstOrNull { it.storageKey == key }
    }
}

/** A deposit into, or withdrawal from, a savings pot; [account] is the money account it moved through. */
data class SavingsEntry(
    val id: UUID = UUID.randomUUID(),
    val kind: SavingsEntryKind,
    val amount: Double,
    val date: Instant = Instant.now(),
    val note: String = "",
    val account: AccountSummary? = null,
) {
    /** How this entry moves the linked money account: a deposit takes money out, a withdrawal returns it. */
    val accountDelta: Double get() = if (kind == SavingsEntryKind.Deposit) -amount else amount
}

/** A savings pot or goal (iOS `SavingsAccount`). */
data class SavingsAccount(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val number: String = "",
    val accountId: UUID,
    val logoData: ByteArray? = null,
    val tint: String,
    val startDate: Instant = Instant.now(),
    val target: Double? = null,
    val targetDate: Instant? = null,
    val note: String = "",
    val openingBalance: Double = 0.0,
    val entries: List<SavingsEntry> = emptyList(),
) {
    val totalDeposited: Double
        get() = openingBalance + entries.filter { it.kind == SavingsEntryKind.Deposit }.sumOf { it.amount }

    val totalWithdrawn: Double
        get() = entries.filter { it.kind == SavingsEntryKind.Withdrawal }.sumOf { it.amount }

    val currentSavings: Double get() = max(totalDeposited - totalWithdrawn, 0.0)

    /** 0...1 toward the target; null when no target is set. */
    val progress: Double?
        get() = target?.takeIf { it > 0 }?.let { min(currentSavings / it, 1.0) }

    val remainingToTarget: Double? get() = target?.let { max(it - currentSavings, 0.0) }

    val isGoalReached: Boolean get() = target?.takeIf { it > 0 }?.let { currentSavings >= it } ?: false

    val sortedEntries: List<SavingsEntry> get() = entries.sortedByDescending { it.date }

    // ByteArray makes the generated equals/hashCode identity-based; compare by content instead.
    override fun equals(other: Any?): Boolean =
        other is SavingsAccount && id == other.id && name == other.name && number == other.number &&
            accountId == other.accountId && logoData.contentEquals(other.logoData) && tint == other.tint &&
            startDate == other.startDate && target == other.target && targetDate == other.targetDate &&
            note == other.note && openingBalance == other.openingBalance && entries == other.entries

    override fun hashCode(): Int = id.hashCode()

    companion object {
        const val SUFFIX = "Savings"

        /** "Emergency" -> "Emergency Savings"; leaves names that already end in "Savings" alone. */
        fun suffixed(name: String): String {
            val trimmed = name.trim()
            if (trimmed.isEmpty()) return ""
            return if (trimmed.lowercase().endsWith(SUFFIX.lowercase())) trimmed else "$trimmed $SUFFIX"
        }
    }
}

private fun ByteArray?.contentEquals(other: ByteArray?): Boolean =
    if (this == null || other == null) this == null && other == null else this.contentEquals(other)

/** A savings pot together with the money account backing it. */
data class SavingsItem(
    val savings: SavingsAccount,
    val account: AccountSummary?,
) {
    val id: UUID get() = savings.id
}

enum class SavingsLogFilter(override val title: String) : PickerOption {
    All("All"),
    Added("Added"),
    Withdrawn("Withdrawn");

    fun includes(kind: SavingsEntryKind): Boolean = when (this) {
        All -> true
        Added -> kind == SavingsEntryKind.Deposit
        Withdrawn -> kind == SavingsEntryKind.Withdrawal
    }
}

data class SavingsLogEntry(
    val entry: SavingsEntry,
    val savingsId: UUID,
    val savingsName: String,
) {
    val id: UUID get() = entry.id
}

/** All log entries of one calendar month. */
data class SavingsLogSection(
    val month: YearMonth,
    val entries: List<SavingsLogEntry>,
) {
    val added: Double get() = entries.filter { it.entry.kind == SavingsEntryKind.Deposit }.sumOf { it.entry.amount }
    val withdrawn: Double get() = entries.filter { it.entry.kind == SavingsEntryKind.Withdrawal }.sumOf { it.entry.amount }
    val net: Double get() = added - withdrawn

    companion object {
        /** Groups by month, newest month first and newest entry first inside each month. */
        fun make(entries: List<SavingsLogEntry>, zone: ZoneId = ZoneId.systemDefault()): List<SavingsLogSection> =
            entries
                .groupBy { YearMonth.from(it.entry.date.atZone(zone)) }
                .map { (month, group) -> SavingsLogSection(month, group.sortedByDescending { it.entry.date }) }
                .sortedByDescending { it.month }
    }
}

data class SavingsBreakdownRow(
    val id: UUID,
    val name: String,
    val tint: String,
    val type: AccountType,
    val amount: Double,
    val share: Double,
    val count: Int,
)

data class SavingsSummary(
    val totalSavings: Double,
    val totalTarget: Double,
    val totalDeposited: Double,
    val totalWithdrawn: Double,
    val count: Int,
    val goalsReached: Int,
    val breakdown: List<SavingsBreakdownRow>,
) {
    val progress: Double? get() = if (totalTarget > 0) min(totalSavings / totalTarget, 1.0) else null

    companion object {
        val Empty = SavingsSummary(0.0, 0.0, 0.0, 0.0, 0, 0, emptyList())

        fun make(savings: List<SavingsAccount>, accounts: List<Account>): SavingsSummary {
            val total = savings.sumOf { it.currentSavings }
            val rows = savings.groupBy { it.accountId }.mapNotNull { (id, group) ->
                val account = accounts.firstOrNull { it.id == id } ?: return@mapNotNull null
                val amount = group.sumOf { it.currentSavings }
                SavingsBreakdownRow(
                    id = id,
                    name = account.name,
                    tint = account.tint,
                    type = account.type,
                    amount = amount,
                    share = if (total > 0) amount / total else 0.0,
                    count = group.size,
                )
            }.sortedByDescending { it.amount }

            return SavingsSummary(
                totalSavings = total,
                totalTarget = savings.sumOf { it.target ?: 0.0 },
                totalDeposited = savings.sumOf { it.totalDeposited },
                totalWithdrawn = savings.sumOf { it.totalWithdrawn },
                count = savings.size,
                goalsReached = savings.count { it.isGoalReached },
                breakdown = rows,
            )
        }
    }
}
