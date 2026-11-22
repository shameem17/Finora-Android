package com.shameem.finora.core.data

import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Groups several repository writes into one atomic commit (iOS `UnitOfWork`). */
interface UnitOfWork {
    suspend fun <T> perform(work: suspend () -> T): T
}

/** For unit tests with in-memory fakes: no batching needed. */
object ImmediateUnitOfWork : UnitOfWork {
    override suspend fun <T> perform(work: suspend () -> T): T = work()
}

/** A change notification that can be held back until the surrounding transaction has committed. */
interface DeferrableSignal {
    fun emitNow()
}

/** Coroutine-context marker for "a unit of work is running": signals raised inside are queued here. */
class UnitOfWorkScope : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UnitOfWorkScope>

    private val pending = LinkedHashSet<DeferrableSignal>()

    /** Queues [signal] (once per unit of work). */
    internal fun defer(signal: DeferrableSignal) {
        synchronized(pending) { pending.add(signal) }
    }

    internal fun flush() {
        val signals = synchronized(pending) { pending.toList().also { pending.clear() } }
        signals.forEach(DeferrableSignal::emitNow)
    }
}

/**
 * Raises [signal] now, or after the current unit of work commits if one is running, so observers always
 * re-read committed data (iOS `PersistenceController.write(notifying:)`).
 */
suspend fun notifyAfterCommit(signal: DeferrableSignal) {
    val scope = currentCoroutineContext()[UnitOfWorkScope]
    if (scope != null) scope.defer(signal) else signal.emitNow()
}

/**
 * Room-backed unit of work: the block runs in one transaction and rolls back if it throws. Nested calls join
 * the outer transaction. Change signals fire once, after the outermost commit.
 */
class RoomUnitOfWork(private val database: RoomDatabase) : UnitOfWork {
    override suspend fun <T> perform(work: suspend () -> T): T {
        if (currentCoroutineContext()[UnitOfWorkScope] != null) {
            return database.withTransaction { work() }
        }
        val scope = UnitOfWorkScope()
        val result = withContext(scope) { database.withTransaction { work() } }
        scope.flush() // only reached on commit; a thrown exception skips it
        return result
    }
}
