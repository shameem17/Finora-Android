package com.shameem.finora.screens.cashflow.repository

import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.categories.model.CategoryKind
import java.util.UUID

interface CashFlowRepository : ChangeNotifying {
    suspend fun fetchEntries(kind: CategoryKind): List<CashFlowEntry>
    suspend fun entry(id: UUID): CashFlowEntry?
    suspend fun add(entry: CashFlowEntry)
    suspend fun update(entry: CashFlowEntry)
    suspend fun delete(id: UUID)
}
