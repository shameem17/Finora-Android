package com.shameem.finora.core.data

import androidx.room.RoomDatabase
import androidx.room.withTransaction

/** Groups several repository writes into one atomic commit (iOS `UnitOfWork`). */
interface UnitOfWork {
    suspend fun <T> perform(work: suspend () -> T): T
}

/** For unit tests with in-memory fakes: no batching needed. */
object ImmediateUnitOfWork : UnitOfWork {
    override suspend fun <T> perform(work: suspend () -> T): T = work()
}

/** Room-backed unit of work: the block runs in one transaction and rolls back if it throws. */
class RoomUnitOfWork(private val database: RoomDatabase) : UnitOfWork {
    override suspend fun <T> perform(work: suspend () -> T): T = database.withTransaction { work() }
}
