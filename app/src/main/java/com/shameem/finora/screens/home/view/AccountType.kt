package com.shameem.finora.screens.home.view

/**
 * Kind of money account (iOS `AccountType`). Persisted by [storageKey] (the exact Swift case name),
 * never by [title], so renaming a title can't orphan stored data.
 */
enum class AccountType(val title: String, val storageKey: String, val symbol: String) {
    Bank("Bank", "bank", "building.columns.fill"),
    Mfs("MFS", "mfs", "iphone.gen3"),
    Online("Online", "online", "globe"),
    Virtual("Virtual", "virtual", "cube.transparent.fill"),
    Card("Card", "card", "creditcard.fill"),
    Savings("Savings", "savings", "chart.line.uptrend.xyaxis"),
    Cash("Cash", "cash", "banknote.fill"),
    Unknown("Unknown", "unknown", "questionmark");

    companion object {
        /** Everything the user may pick when creating an account. */
        val selectable: List<AccountType> = entries.filter { it != Unknown }

        fun fromStorageKey(key: String): AccountType? = entries.firstOrNull { it.storageKey == key }
    }
}
