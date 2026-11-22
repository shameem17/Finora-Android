package com.shameem.finora.screens.transaction

import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.home.model.Account
import java.util.UUID

interface AccountRepository : ChangeNotifying {
    suspend fun fetchAccounts(): List<Account>

    suspend fun account(id: UUID): Account?

    suspend fun add(account: Account)

    /**
     * Saves edited details (name, number, type, logo, colour, initial balance). Recorded movement is
     * preserved, so the current balance stays calculated.
     */
    suspend fun update(account: Account)

    /** Debits `amount + charge` from the source and credits `amount` to the destination. */
    suspend fun apply(transfer: Transfer)

    /** Adds [delta] (negative to debit) to one account's balance. */
    suspend fun adjustBalance(id: UUID, delta: Double)
}
