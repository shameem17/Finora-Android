package com.shameem.finora.screens.cashflow.service

import com.shameem.finora.core.data.ImmediateUnitOfWork
import com.shameem.finora.core.data.UnitOfWork
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.cashflow.repository.CashFlowRepository
import com.shameem.finora.screens.categories.repository.CategoryRepository
import com.shameem.finora.screens.transaction.AccountRepository
import java.util.UUID

/** Records income / expense and keeps the affected account balance in step, atomically. */
interface CashFlowService {
    suspend fun record(entry: CashFlowEntry)
    suspend fun update(entry: CashFlowEntry)
    suspend fun delete(id: UUID)
}

class DefaultCashFlowService(
    private val entries: CashFlowRepository,
    private val accounts: AccountRepository,
    private val unitOfWork: UnitOfWork = ImmediateUnitOfWork,
) : CashFlowService {

    override suspend fun record(entry: CashFlowEntry) {
        unitOfWork.perform {
            accounts.adjustBalance(entry.accountId, entry.accountDelta)
            entries.add(entry)
        }
    }

    override suspend fun update(entry: CashFlowEntry) {
        unitOfWork.perform {
            val old = entries.entry(entry.id) ?: return@perform
            // Undo the old effect, then apply the new one (the account may have changed).
            accounts.adjustBalance(old.accountId, -old.accountDelta)
            accounts.adjustBalance(entry.accountId, entry.accountDelta)
            entries.update(entry)
        }
    }

    override suspend fun delete(id: UUID) {
        unitOfWork.perform {
            val old = entries.entry(id) ?: return@perform
            accounts.adjustBalance(old.accountId, -old.accountDelta)
            entries.delete(id)
        }
    }
}

/** Everything the income / expense screens need (iOS `CashFlowDependencies`). */
data class CashFlowDependencies(
    val entries: CashFlowRepository,
    val categories: CategoryRepository,
    val accounts: AccountRepository,
    val service: CashFlowService,
)
