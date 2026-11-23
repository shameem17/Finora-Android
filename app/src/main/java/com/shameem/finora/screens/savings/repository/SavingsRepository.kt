package com.shameem.finora.screens.savings.repository

import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.savings.model.SavingsAccount
import java.util.UUID

interface SavingsRepository : ChangeNotifying {
    suspend fun fetchSavings(): List<SavingsAccount>
    suspend fun savings(id: UUID): SavingsAccount?
    suspend fun add(savings: SavingsAccount)

    /** Replaces the stored pot with [savings], including its full list of entries. */
    suspend fun update(savings: SavingsAccount)

    /** Deletes the pot and its entries. */
    suspend fun delete(id: UUID)
}
