package com.shameem.finora.core.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Locale-independent number formatting with lakh/crore grouping (`1,25,450.75`), matching the iOS
 * `en_IN` formatter and deterministic in unit tests.
 */
object NumberGrouping {

    /** Formats [value] with between [minFractionDigits] and [maxFractionDigits] decimals. */
    fun format(value: Double, minFractionDigits: Int = 0, maxFractionDigits: Int = minFractionDigits): String {
        val rounded = BigDecimal.valueOf(value).setScale(maxFractionDigits, RoundingMode.HALF_EVEN)
        val plain = rounded.abs().toPlainString()
        val integerPart = plain.substringBefore('.')
        val fraction = plain.substringAfter('.', "").trimEnd('0').padEnd(minFractionDigits, '0')
        return buildString {
            if (rounded.signum() < 0) append('-')
            append(groupIndian(integerPart))
            if (fraction.isNotEmpty()) append('.').append(fraction)
        }
    }

    /** Last three digits, then groups of two: `1234567` -> `12,34,567`. */
    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits
        val head = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        return "$head,${digits.takeLast(3)}"
    }
}

/** iOS `TakaFormatter` (from DonutCard.swift): symbol + exactly [fractionDigits] decimals. */
object TakaFormatter {
    fun string(value: Double, fractionDigits: Int = 0): String =
        AppCurrency.current.symbol + NumberGrouping.format(value, fractionDigits, fractionDigits)
}

/** iOS `CurrencyFormatter`: symbol followed by the grouped number (sign after the symbol, as on iOS). */
object CurrencyFormatter {
    /** Symbol of the current currency, e.g. "৳". */
    val symbol: String get() = AppCurrency.current.symbol

    fun taka(value: Double, showFraction: Boolean = false): String =
        symbol + NumberGrouping.format(value, 0, if (showFraction) 2 else 0)
}

/** Sanitising and parsing of amounts typed into text fields (iOS `DecimalInput`). */
object DecimalInput {

    fun sanitize(raw: String, maxFractionDigits: Int = 2, maxLength: Int = 12): String {
        val result = StringBuilder()
        var hasSeparator = false
        var fractionCount = 0
        for (ch in raw.replace(',', '.')) {
            if (ch in '0'..'9') {
                if (hasSeparator) {
                    if (fractionCount >= maxFractionDigits) continue
                    fractionCount++
                }
                result.append(ch)
            } else if (ch == '.' && !hasSeparator) {
                hasSeparator = true
                result.append(ch)
            }
            if (result.length >= maxLength) break
        }
        return result.toString()
    }

    fun value(text: String): Double = text.toDoubleOrNull() ?: 0.0

    /** Editable text for an amount: whole numbers without decimals, otherwise up to two. */
    fun text(value: Double): String = NumberGrouping.format(value, 0, 2).replace(",", "")
}
