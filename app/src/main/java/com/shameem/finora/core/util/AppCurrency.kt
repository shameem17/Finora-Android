package com.shameem.finora.core.util

/**
 * Currency shown across the app (iOS `AppCurrency`). [current] is kept in sync by `AppSettings`
 * so plain formatters don't need the settings object injected.
 */
enum class AppCurrency(val symbol: String, val localeIdentifier: String) {
    /** Bangladeshi taka, grouped in lakh / crore: 1,25,000. */
    Bdt(symbol = "৳", localeIdentifier = "en_IN");

    companion object {
        @Volatile
        var current: AppCurrency = Bdt
    }
}
