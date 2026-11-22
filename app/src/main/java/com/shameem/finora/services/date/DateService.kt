package com.shameem.finora.services.date

import java.time.Clock
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DateData(
    val day: String,
    val month: String,
    val year: String,
)

interface DateService {
    fun currentDate(): DateData
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
}
