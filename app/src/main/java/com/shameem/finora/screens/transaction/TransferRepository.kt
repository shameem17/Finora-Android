package com.shameem.finora.screens.transaction

import com.shameem.finora.core.data.ChangeNotifying

interface TransferRepository : ChangeNotifying {
    /** Newest first. */
    suspend fun fetchTransfers(): List<Transfer>

    suspend fun add(transfer: Transfer)
}
