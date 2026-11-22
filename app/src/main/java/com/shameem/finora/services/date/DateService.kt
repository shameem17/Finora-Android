package com.shameem.finora.services.date

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class DateData(
    val day: String,
    val month: String,
    val year: String,
)

data class MonthProgress(
    val elapsedDays: Int,
    val totalDays: Int,
)

interface DateService {
    fun currentDate(): DateData
    fun monthProgress(): MonthProgress
}

class SystemDateService(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val localeProvider: () -> Locale = { Locale.getDefault() },
) : DateService {

    override fun currentDate(): DateData {
        val today = LocalDate.now(clock)
        val locale = localeProvider()
        return DateData(
            day = today.dayOfWeek.getDisplayName(TextStyle.FULL, locale),
            month = today.month.getDisplayName(TextStyle.FULL, locale),
            year = today.year.toString(),
        )
    }

    override fun monthProgress(): MonthProgress {
        val today = LocalDate.now(clock)
        return MonthProgress(elapsedDays = today.dayOfMonth, totalDays = today.lengthOfMonth())
    }
}

/** "12 Nov 2026": the one date style used in lists and detail rows (iOS `Date.shortDisplay`). */
fun Instant.shortDisplay(zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
    atZone(zone).toLocalDate().shortDisplay(locale)

fun LocalDate.shortDisplay(locale: Locale = Locale.getDefault()): String =
    "$dayOfMonth ${month.getDisplayName(TextStyle.SHORT, locale)} $year"
