package com.shameem.finora.screens.transaction

import com.shameem.finora.core.ui.PickerOption
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.UUID

/** Lightweight copy of an account, kept inside records so history survives account edits. */
data class AccountSummary(
    val id: UUID,
    val name: String,
    val tint: String,
    val type: AccountType,
) {
    constructor(account: Account) : this(account.id, account.name, account.tint, account.type)
}

data class Transfer(
    val id: UUID,
    val from: AccountSummary,
    val to: AccountSummary,
    val amount: Double,
    val charge: Double,
    val date: Instant,
    val reason: String,
) {
    /** What leaves the source account: the amount plus any fee. */
    val totalDebited: Double get() = amount + charge
}

data class TransferSummary(
    val totalAmount: Double = 0.0,
    val totalCharges: Double = 0.0,
    val count: Int = 0,
) {
    companion object {
        val Empty = TransferSummary()
    }
}

/** Transfers made on one day. [id] is the start of that day. */
data class TransferSection(
    val id: Instant,
    val title: String,
    val transfers: List<Transfer>,
)

enum class TransferPeriod(override val title: String) : PickerOption {
    Week("Week"),
    Month("Month"),
    Year("Year"),
    All("All");

    /** Human-readable range shown next to the card title. */
    fun subtitle(now: LocalDate = LocalDate.now(), locale: Locale = Locale.getDefault()): String = when (this) {
        Week -> {
            val weekFields = WeekFields.of(locale)
            val start = now.with(weekFields.dayOfWeek(), 1)
            val end = start.plusDays(6)
            "${start.dayMonth(locale)} – ${end.dayMonth(locale)}"
        }
        Month -> "${now.month.getDisplayName(TextStyle.FULL, locale)} ${now.year}"
        Year -> now.year.toString()
        All -> "All time"
    }

    /** Whether [date] falls inside the current week / month / year (always true for [All]). */
    fun contains(
        date: Instant,
        zone: ZoneId = ZoneId.systemDefault(),
        now: LocalDate = LocalDate.now(zone),
        locale: Locale = Locale.getDefault(),
    ): Boolean {
        val day = date.atZone(zone).toLocalDate()
        return when (this) {
            Week -> {
                val weekFields = WeekFields.of(locale)
                day.get(weekFields.weekBasedYear()) == now.get(weekFields.weekBasedYear()) &&
                    day.get(weekFields.weekOfWeekBasedYear()) == now.get(weekFields.weekOfWeekBasedYear())
            }
            Month -> day.year == now.year && day.month == now.month
            Year -> day.year == now.year
            All -> true
        }
    }
}

/** "12 Nov" style used in the week subtitle. */
private fun LocalDate.dayMonth(locale: Locale): String =
    "$dayOfMonth ${month.getDisplayName(TextStyle.SHORT, locale)}"
