package com.shameem.finora.services.persistence

import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.cashflow.repository.CashFlowRepository
import com.shameem.finora.screens.categories.model.CategoryKind
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RoomCashFlowRepository(database: FinoraDatabase) : CashFlowRepository {

    private val dao = database.cashFlowDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchEntries(kind: CategoryKind): List<CashFlowEntry> =
        dao.byKind(kind.storageKey).map(CashFlowEntryEntity::toDomain)

    override suspend fun entry(id: UUID): CashFlowEntry? = dao.byId(id.toString())?.toDomain()

    override suspend fun add(entry: CashFlowEntry) {
        dao.insert(CashFlowEntryEntity.from(entry))
        signal.notifyChanged()
    }

    override suspend fun update(entry: CashFlowEntry) {
        dao.updateDetails(
            id = entry.id.toString(),
            categoryId = entry.categoryId.toString(),
            accountId = entry.accountId.toString(),
            amount = entry.amount,
            date = entry.date.toEpochMilli(),
            note = entry.note,
        )
        signal.notifyChanged()
    }

    override suspend fun delete(id: UUID) {
        dao.delete(id.toString())
        signal.notifyChanged()
    }
}
