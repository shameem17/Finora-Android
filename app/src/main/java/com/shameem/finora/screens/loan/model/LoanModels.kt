package com.shameem.finora.screens.loan.model

import com.shameem.finora.core.ui.PickerOption
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** Direction of a loan. Persisted by [storageKey] (the iOS case name). */
enum class LoanKind(override val title: String, val storageKey: String) : PickerOption {
    Lent("Loan", "lent"),
    Borrowed("Borrow", "borrowed");

    /** Effect on the linked account when the loan is made: lending sends money out, borrowing brings it in. */
    fun principalDelta(amount: Double): Double = if (this == Lent) -amount else amount

    /** Effect on the linked account of a repayment: you receive when you lent, you pay when you borrowed. */
    fun repaymentDelta(amount: Double): Double = if (this == Lent) amount else -amount

    companion object {
        fun fromStorageKey(key: String): LoanKind? = entries.firstOrNull { it.storageKey == key }
    }
}

data class Repayment(
    val id: UUID = UUID.randomUUID(),
    val amount: Double,
    val date: Instant = Instant.now(),
    val account: AccountSummary,
    val note: String? = null,
)

/** One scheduled part of a loan's repayment plan. */
data class Installment(
    val number: Int,
    val dueDate: Instant,
    val amount: Double,
    val paid: Double,
) {
    val id: Int get() = number
    val remaining: Double get() = max(amount - paid, 0.0)
    val isPaid: Boolean get() = remaining < Loan.TOLERANCE
}

sealed interface LoanStatus {
    data object Settled : LoanStatus
    data class Overdue(val days: Int) : LoanStatus

    /** [inDays] = 0 means due today. */
    data class Due(val inDays: Int) : LoanStatus

    /** No due date set. */
    data object Open : LoanStatus

    val overdueDays: Int? get() = (this as? Overdue)?.days

    /** Sort key: soonest due first; everything that isn't "due in N days" sorts last. */
    val dueRank: Int get() = (this as? Due)?.inDays ?: Int.MAX_VALUE
}

/** Money lent to, or borrowed from, a [Person] (iOS `Loan`). */
data class Loan(
    val id: UUID = UUID.randomUUID(),
    val kind: LoanKind,
    val personId: UUID,
    val account: AccountSummary,
    val principal: Double,
    val interest: Double = 0.0,
    val date: Instant = Instant.now(),
    val dueDate: Instant? = null,
    val installmentCount: Int = 1,
    val note: String? = null,
    val repayments: List<Repayment> = emptyList(),
) {
    val totalPayable: Double get() = principal + interest
    val totalRepaid: Double get() = repayments.sumOf { it.amount }
    val outstanding: Double get() = max(totalPayable - totalRepaid, 0.0)
    val isSettled: Boolean get() = outstanding < TOLERANCE
    val progress: Double get() = if (totalPayable > 0) min(totalRepaid / totalPayable, 1.0) else 0.0
    val lastActivity: Instant get() = repayments.maxOfOrNull { it.date }?.let { maxOf(date, it) } ?: date

    /**
     * The repayment plan: [installmentCount] monthly parts starting at [dueDate]. Amounts are rounded down to
     * the paisa, with the last part absorbing the remainder so they always add up to [totalPayable]. Payments
     * fill the earliest instalments first. Empty when there is no due date.
     */
    fun installments(zone: ZoneId = ZoneId.systemDefault()): List<Installment> {
        val first = dueDate ?: return emptyList()
        val count = max(installmentCount, 1)
        val total = totalPayable
        val base = floor(total / count * 100) / 100
        var unallocatedPayment = totalRepaid
        var allocated = 0.0
        val firstDue = first.atZone(zone)

        return (0 until count).map { index ->
            val amount = if (index == count - 1) ((total - allocated) * 100).roundToLong() / 100.0 else base
            allocated += amount
            val paid = min(max(unallocatedPayment, 0.0), amount)
            unallocatedPayment -= paid
            Installment(
                number = index + 1,
                dueDate = firstDue.plusMonths(index.toLong()).toInstant(),
                amount = amount,
                paid = paid,
            )
        }
    }

    fun nextInstallment(zone: ZoneId = ZoneId.systemDefault()): Installment? =
        installments(zone).firstOrNull { !it.isPaid }

    /** Unpaid instalment money falling due before [limit]. */
    fun amountDue(limit: Instant, zone: ZoneId = ZoneId.systemDefault()): Double {
        if (isSettled) return 0.0
        return installments(zone).filter { it.dueDate < limit }.sumOf { it.remaining }
    }

    fun status(clock: Clock = Clock.systemDefaultZone()): LoanStatus {
        if (isSettled) return LoanStatus.Settled
        val zone = clock.zone
        val next = nextInstallment(zone) ?: return LoanStatus.Open
        val today = LocalDate.now(clock)
        val due = next.dueDate.atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(today, due).toInt()
        return if (days < 0) LoanStatus.Overdue(-days) else LoanStatus.Due(days)
    }

    companion object {
        const val TOLERANCE = 0.005
        const val MAX_INSTALLMENTS = 60
    }
}

/** A loan with its person and computed status, as shown in lists. */
data class LoanItem(
    val loan: Loan,
    val person: Person,
    val status: LoanStatus,
) {
    val id: UUID get() = loan.id

    companion object {
        fun of(loan: Loan, person: Person?, clock: Clock = Clock.systemDefaultZone()) = LoanItem(
            loan = loan,
            person = person ?: Person.unknown(loan.personId),
            status = loan.status(clock),
        )
    }
}
