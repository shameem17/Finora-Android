package com.shameem.finora.core.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formats amounts in Bangladeshi Taka with lakh/crore digit grouping (`1,25,450.75`).
 * Deterministic and locale-independent, so it behaves identically in unit tests.
 */
object TakaFormatter {

    const val SYMBOL = "৳"

    fun format(
        value: Double,
        maxFractionDigits: Int = 0,
        minFractionDigits: Int = 0,
    ): String {
        val rounded = BigDecimal.valueOf(value).setScale(maxFractionDigits, RoundingMode.HALF_EVEN)
        val plain = rounded.abs().toPlainString()
        val integerPart = plain.substringBefore('.')
        val fractionPart = plain.substringAfter('.', "")
            .trimEnd('0')
            .padEnd(minFractionDigits, '0')

        return buildString {
            if (rounded.signum() < 0) append('-')
            append(SYMBOL)
            append(groupIndian(integerPart))
            if (fractionPart.isNotEmpty()) append('.').append(fractionPart)
        }
    }

    /** Last three digits, then groups of two: `1234567` -> `12,34,567`. */
    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits
        val head = digits.dropLast(3)
        val tail = digits.takeLast(3)
        val groupedHead = head.reversed().chunked(2).joinToString(",").reversed()
        return "$groupedHead,$tail"
    }
}
