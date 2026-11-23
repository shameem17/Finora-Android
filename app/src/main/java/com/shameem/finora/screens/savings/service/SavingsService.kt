package com.shameem.finora.screens.savings.service

import com.shameem.finora.core.data.ImmediateUnitOfWork
import com.shameem.finora.core.data.UnitOfWork
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.repository.SavingsRepository
import com.shameem.finora.screens.transaction.AccountRepository
import java.util.UUID

/** Savings operations that also move money in the linked account, atomically. */
interface SavingsService {
    suspend fun create(savings: SavingsAccount)
    suspend fun update(savings: SavingsAccount)

    /** Deletes the pot; with [returnRemaining] its balance goes back to the linked account. */
    suspend fun delete(savingsId: UUID, returnRemaining: Boolean)

    suspend fun deposit(entry: SavingsEntry, savingsId: UUID)
    suspend fun withdraw(entry: SavingsEntry, savingsId: UUID)

    /** Returns false (changing nothing) if the edit would leave the pot with a negative balance. */
    suspend fun updateEntry(entry: SavingsEntry, savingsId: UUID): Boolean

    /** Returns false (changing nothing) if removing the entry would leave a negative balance. */
    suspend fun deleteEntry(id: UUID, savingsId: UUID): Boolean
}

class DefaultSavingsService(
    private val savings: SavingsRepository,
    private val accounts: AccountRepository,
    private val unitOfWork: UnitOfWork = ImmediateUnitOfWork,
) : SavingsService {

    override suspend fun create(savings: SavingsAccount) {
        unitOfWork.perform { this.savings.add(savings) }
    }

    override suspend fun update(savings: SavingsAccount) {
        unitOfWork.perform {
            // Only the editable details change; entries and opening balance stay as stored.
            val stored = this.savings.savings(savings.id) ?: return@perform
            this.savings.update(
                stored.copy(
                    name = savings.name,
                    number = savings.number,
                    accountId = savings.accountId,
                    logoData = savings.logoData,
                    tint = savings.tint,
                    startDate = savings.startDate,
                    target = savings.target,
                    targetDate = savings.targetDate,
                    note = savings.note,
                ),
            )
        }
    }

    override suspend fun delete(savingsId: UUID, returnRemaining: Boolean) {
        unitOfWork.perform {
            val item = savings.savings(savingsId) ?: return@perform
            if (returnRemaining && item.currentSavings > 0) {
                accounts.adjustBalance(item.accountId, item.currentSavings)
            }
            savings.delete(savingsId)
        }
    }

    override suspend fun deposit(entry: SavingsEntry, savingsId: UUID) {
        unitOfWork.perform {
            val item = savings.savings(savingsId) ?: return@perform
            apply(entry, sign = 1.0)
            savings.update(item.copy(entries = item.entries + entry))
        }
    }

    override suspend fun withdraw(entry: SavingsEntry, savingsId: UUID) {
        unitOfWork.perform {
            val item = savings.savings(savingsId) ?: return@perform
            if (entry.amount > item.currentSavings + 0.005) return@perform
            apply(entry, sign = 1.0)
            savings.update(item.copy(entries = item.entries + entry))
        }
    }

    override suspend fun deleteEntry(id: UUID, savingsId: UUID): Boolean = unitOfWork.perform {
        val item = savings.savings(savingsId) ?: return@perform false
        val entry = item.entries.firstOrNull { it.id == id } ?: return@perform false
        val updated = item.copy(entries = item.entries.filterNot { it.id == id })
        if (updated.totalDeposited - updated.totalWithdrawn <= -0.005) return@perform false
        apply(entry, sign = -1.0)
        savings.update(updated)
        true
    }

    override suspend fun updateEntry(entry: SavingsEntry, savingsId: UUID): Boolean = unitOfWork.perform {
        val item = savings.savings(savingsId) ?: return@perform false
        val index = item.entries.indexOfFirst { it.id == entry.id }
        if (index < 0) return@perform false
        val old = item.entries[index]
        val updated = item.copy(entries = item.entries.toMutableList().also { it[index] = entry })
        if (updated.totalDeposited - updated.totalWithdrawn <= -0.005) return@perform false
        apply(old, sign = -1.0)
        apply(entry, sign = 1.0)
        savings.update(updated)
        true
    }

    private suspend fun apply(entry: SavingsEntry, sign: Double) {
        val account = entry.account ?: return
        accounts.adjustBalance(account.id, entry.accountDelta * sign)
    }
}

data class SavingsDependencies(
    val savings: SavingsRepository,
    val accounts: AccountRepository,
    val service: SavingsService,
)
