package com.shameem.finora.core.util

import com.shameem.finora.core.ui.PickerOption
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/** The Week / Month / Year / All filter shared by the transfer history, income and expense (iOS `ReportPeriod`). */
enum class ReportPeriod(override val title: String) : PickerOption {
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

    /** Exclusive end of the current period (start of the next one); `null` for [All]. */
    fun endDate(
        zone: ZoneId = ZoneId.systemDefault(),
        now: LocalDate = LocalDate.now(zone),
        locale: Locale = Locale.getDefault(),
    ): Instant? {
        val nextStart = when (this) {
            Week -> now.with(WeekFields.of(locale).dayOfWeek(), 1).plusDays(7)
            Month -> now.withDayOfMonth(1).plusMonths(1)
            Year -> now.withDayOfYear(1).plusYears(1)
            All -> return null
        }
        return nextStart.atStartOfDay(zone).toInstant()
    }
}

/** "12 Nov" style used in the week subtitle. */
private fun LocalDate.dayMonth(locale: Locale): String =
    "$dayOfMonth ${month.getDisplayName(TextStyle.SHORT, locale)}"
