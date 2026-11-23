package com.shameem.finora.screens.loan.model

import java.util.UUID

enum class PersonCategory(val title: String, val storageKey: String, val icon: String) {
    Individual("Person", "individual", "person.fill"),
    Bank("Bank", "bank", "building.columns.fill"),
    Wallet("Mobile wallet", "wallet", "iphone.gen3"),
    Other("Other", "other", "ellipsis");

    companion object {
        fun fromStorageKey(key: String): PersonCategory? = entries.firstOrNull { it.storageKey == key }
    }
}

/** Someone you lend to or borrow from (iOS `Person`). */
data class Person(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val category: PersonCategory = PersonCategory.Individual,
    val phone: String? = null,
    val address: String? = null,
) {
    /** Up to two initials of the name ("Rahim Uddin" -> "RU"), or "?". */
    val initials: String
        get() = name.split(' ')
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().toString() }
            .uppercase()
            .ifEmpty { "?" }

    companion object {
        /** Stand-in for a person whose record has been deleted. */
        fun unknown(id: UUID) = Person(id = id, name = "Unknown", category = PersonCategory.Other)
    }
}
