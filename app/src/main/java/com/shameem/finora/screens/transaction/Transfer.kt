package com.shameem.finora.screens.transaction

import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountType
import java.time.Instant
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
