package com.shameem.finora.services.persistence

import androidx.room.withTransaction
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RoomSavingsRepository(private val database: FinoraDatabase) : SavingsRepository {

    private val dao = database.savingsDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchSavings(): List<SavingsAccount> {
        val entriesByPot = dao.allEntries().groupBy({ it.savingsId }, { it.toDomain() })
        return dao.all().map { it.toDomain(entriesByPot[it.id].orEmpty()) }
    }

    override suspend fun savings(id: UUID): SavingsAccount? {
        val stored = dao.byId(id.toString()) ?: return null
        return stored.toDomain(dao.entriesOf(stored.id).map(SavingsEntryEntity::toDomain))
    }

    override suspend fun add(savings: SavingsAccount) {
        database.withTransaction {
            dao.insert(SavingsEntity.from(savings))
            writeEntries(savings)
        }
        signal.notifyChanged()
    }

    override suspend fun update(savings: SavingsAccount) {
        database.withTransaction {
            dao.updateDetails(
                id = savings.id.toString(),
                name = savings.name,
                number = savings.number,
                accountId = savings.accountId.toString(),
                logoData = savings.logoData,
                tint = savings.tint,
                startDate = savings.startDate.toEpochMilli(),
                target = savings.target,
                targetDate = savings.targetDate?.toEpochMilli(),
                note = savings.note,
                openingBalance = savings.openingBalance,
            )
            writeEntries(savings)
        }
        signal.notifyChanged()
    }

    override suspend fun delete(id: UUID) {
        dao.delete(id.toString()) // entries cascade
        signal.notifyChanged()
    }

    /** Makes the stored entries exactly [savings].entries, in order. */
    private suspend fun writeEntries(savings: SavingsAccount) {
        val potId = savings.id.toString()
        dao.deleteEntriesOf(potId)
        dao.upsertEntries(savings.entries.mapIndexed { index, entry -> SavingsEntryEntity.from(entry, savings.id, index) })
    }
}
