package com.shameem.finora.services.persistence

import androidx.room.withTransaction
import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.core.data.DeferrableSignal
import com.shameem.finora.core.data.notifyAfterCommit
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.transaction.AccountRepository
import com.shameem.finora.screens.transaction.Transfer
import com.shameem.finora.screens.transaction.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * Backs a repository's `changes` stream. Emits after a write commits; inside a unit of work the emission
 * is held until the outermost transaction commits, so observers always re-read stored truth.
 */
internal class ChangeSignal : ChangeNotifying, DeferrableSignal {
    private val subject = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    override val changes: Flow<Unit> = subject.asSharedFlow()

    override fun emitNow() {
        subject.tryEmit(Unit)
    }

    suspend fun notifyChanged() = notifyAfterCommit(this)
}

class RoomAccountRepository(
    private val database: FinoraDatabase,
) : AccountRepository {

    private val dao = database.accountDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchAccounts(): List<Account> = dao.all().map(AccountEntity::toDomain)

    override suspend fun account(id: UUID): Account? = dao.byId(id.toString())?.toDomain()

    override suspend fun add(account: Account) {
        dao.insert(AccountEntity.from(account))
        signal.notifyChanged()
    }

    override suspend fun update(account: Account) {
        dao.updateDetails(
            id = account.id.toString(),
            name = account.name,
            number = account.number,
            typeKey = account.type.storageKey,
            logo = account.logo,
            logoData = account.logoData,
            tint = account.tint,
            initialBalance = account.initialBalance,
        )
        signal.notifyChanged()
    }

    override suspend fun apply(transfer: Transfer) {
        database.withTransaction {
            dao.addMovement(transfer.from.id.toString(), -transfer.totalDebited)
            dao.addMovement(transfer.to.id.toString(), transfer.amount)
        }
        signal.notifyChanged()
    }

    override suspend fun adjustBalance(id: UUID, delta: Double) {
        dao.addMovement(id.toString(), delta)
        signal.notifyChanged()
    }
}

class RoomTransferRepository(
    database: FinoraDatabase,
) : TransferRepository {

    private val dao = database.transferDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    override suspend fun fetchTransfers(): List<Transfer> = dao.allNewestFirst().map(TransferEntity::toDomain)

    override suspend fun add(transfer: Transfer) {
        dao.insert(TransferEntity.from(transfer))
        signal.notifyChanged()
    }
}
