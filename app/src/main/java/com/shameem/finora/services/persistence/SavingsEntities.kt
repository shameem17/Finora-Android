package com.shameem.finora.services.persistence

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.home.view.AccountType
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.repository.SavingsRepository
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.room.withTransaction
import java.time.Instant
import java.util.UUID

// MARK: - Entities (iOS SavingsEntity / SavingsEntryEntity)

@Entity(tableName = "savings")
data class SavingsEntity(
    @PrimaryKey val id: String,
    val name: String,
    val number: String,
    val accountId: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val logoData: ByteArray?,
    val tint: String,
    val startDateMillis: Long,
    val target: Double?,
    val targetDateMillis: Long?,
    val note: String,
    val openingBalance: Double,
    val createdAtMillis: Long,
) {
    // ByteArray needs content equality; identity is the primary key anyway.
    override fun equals(other: Any?): Boolean =
        other is SavingsEntity && id == other.id && name == other.name && number == other.number &&
            accountId == other.accountId && logoData.contentEqualsNullable(other.logoData) &&
            tint == other.tint && startDateMillis == other.startDateMillis && target == other.target &&
            targetDateMillis == other.targetDateMillis && note == other.note &&
            openingBalance == other.openingBalance && createdAtMillis == other.createdAtMillis

    override fun hashCode(): Int = id.hashCode()
}

private fun ByteArray?.contentEqualsNullable(other: ByteArray?): Boolean =
    if (this == null || other == null) this == null && other == null else this.contentEquals(other)

/**
 * One deposit / withdrawal. The linked account snapshot (iOS `AccountSnapshot`) is flattened into
 * the `account*` columns; all four are null when the money moved outside Finora accounts.
 * `position` keeps the order entries were stored in (iOS `order`).
 */
@Entity(
    tableName = "savings_entries",
    foreignKeys = [
        ForeignKey(
            entity = SavingsEntity::class,
            parentColumns = ["id"],
            childColumns = ["savingsId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("savingsId")],
)
data class SavingsEntryEntity(
    @PrimaryKey val id: String,
    val savingsId: String,
    val kindKey: String,
    val amount: Double,
    val dateMillis: Long,
    val note: String,
    val accountId: String?,
    val accountName: String?,
    val accountTint: String?,
    val accountTypeKey: String?,
    val position: Int,
)

/** A savings row with its entries, for one-shot reads and for observing both tables. */
data class SavingsWithEntries(
    @Embedded val savings: SavingsEntity,
    @Relation(parentColumn = "id", entityColumn = "savingsId")
    val entries: List<SavingsEntryEntity>,
)

// MARK: - DAOs

@Dao
interface SavingsDao {

    @Insert
    suspend fun insert(entity: SavingsEntity)

    @Update
    suspend fun update(entity: SavingsEntity)

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM savings WHERE id = :id")
    suspend fun find(id: String): SavingsEntity?

    @Transaction
    @Query("SELECT * FROM savings ORDER BY createdAtMillis")
    suspend fun allWithEntries(): List<SavingsWithEntries>

    @Transaction
    @Query("SELECT * FROM savings WHERE id = :id")
    suspend fun withEntries(id: String): SavingsWithEntries?

    /** Re-emits whenever the savings or savings_entries table changes. */
    @Transaction
    @Query("SELECT * FROM savings ORDER BY createdAtMillis")
    fun observeAllWithEntries(): Flow<List<SavingsWithEntries>>
}

@Dao
interface SavingsEntryDao {

    @Insert
    suspend fun insertAll(entries: List<SavingsEntryEntity>)

    @Update
    suspend fun updateAll(entries: List<SavingsEntryEntity>)

    @Query("SELECT id FROM savings_entries WHERE savingsId = :savingsId")
    suspend fun idsOf(savingsId: String): List<String>

    @Query("DELETE FROM savings_entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)
}

// MARK: - Mapping

private fun SavingsAccount.toEntity(createdAtMillis: Long): SavingsEntity = SavingsEntity(
    id = id.toString(),
    name = name,
    number = number,
    accountId = accountId.toString(),
    logoData = logoData,
    tint = tint,
    startDateMillis = startDate.toEpochMilli(),
    target = target,
    targetDateMillis = targetDate?.toEpochMilli(),
    note = note,
    openingBalance = openingBalance,
    createdAtMillis = createdAtMillis,
)

private fun SavingsEntry.toEntity(savingsId: UUID, position: Int): SavingsEntryEntity = SavingsEntryEntity(
    id = id.toString(),
    savingsId = savingsId.toString(),
    kindKey = kind.storageKey,
    amount = amount,
    dateMillis = date.toEpochMilli(),
    note = note,
    accountId = account?.id?.toString(),
    accountName = account?.name,
    accountTint = account?.tint,
    accountTypeKey = account?.type?.storageKey,
    position = position,
)

private fun SavingsEntryEntity.toDomain(): SavingsEntry {
    val snapshot = accountId?.let { storedId ->
        AccountSummary(
            id = UUID.fromString(storedId),
            name = accountName.orEmpty(),
            tint = accountTint.orEmpty(),
            type = accountTypeKey?.let(AccountType::fromStorageKey) ?: AccountType.Unknown,
        )
    }
    return SavingsEntry(
        id = UUID.fromString(id),
        kind = SavingsEntryKind.fromStorageKey(kindKey) ?: SavingsEntryKind.Deposit,
        amount = amount,
        date = Instant.ofEpochMilli(dateMillis),
        note = note,
        account = snapshot,
    )
}

private fun SavingsWithEntries.toDomain(): SavingsAccount = SavingsAccount(
    id = UUID.fromString(savings.id),
    name = savings.name,
    number = savings.number,
    accountId = UUID.fromString(savings.accountId),
    logoData = savings.logoData,
    tint = savings.tint,
    startDate = Instant.ofEpochMilli(savings.startDateMillis),
    target = savings.target,
    targetDate = savings.targetDateMillis?.let(Instant::ofEpochMilli),
    note = savings.note,
    openingBalance = savings.openingBalance,
    entries = entries.sortedBy { it.position }.map { it.toDomain() },
)

// MARK: - Repository (iOS SwiftDataSavingsRepository)

class RoomSavingsRepository(private val database: FinoraDatabase) : SavingsRepository {

    private val savingsDao get() = database.savingsDao()
    private val entryDao get() = database.savingsEntryDao()

    /** Room re-runs the observed query after every committed write to either table. */
    override val changes: Flow<Unit> = savingsDao.observeAllWithEntries().map { }

    override suspend fun fetchSavings(): List<SavingsAccount> =
        savingsDao.allWithEntries().map { it.toDomain() }

    override suspend fun savings(id: UUID): SavingsAccount? =
        savingsDao.withEntries(id.toString())?.toDomain()

    override suspend fun add(savings: SavingsAccount) {
        database.withTransaction {
            savingsDao.insert(savings.toEntity(createdAtMillis = System.currentTimeMillis()))
            syncEntries(savings)
        }
    }

    override suspend fun update(savings: SavingsAccount) {
        database.withTransaction {
            val stored = savingsDao.find(savings.id.toString()) ?: return@withTransaction
            savingsDao.update(savings.toEntity(createdAtMillis = stored.createdAtMillis))
            syncEntries(savings)
        }
    }

    override suspend fun delete(id: UUID) {
        savingsDao.delete(id.toString()) // entries cascade
    }

    /** Makes the stored entries match `savings.entries` (insert new, update changed, delete removed). */
    private suspend fun syncEntries(savings: SavingsAccount) {
        val parentId = savings.id.toString()
        val incoming = savings.entries.mapIndexed { index, entry -> entry.toEntity(savings.id, index) }
        val storedIds = entryDao.idsOf(parentId).toSet()
        val incomingIds = incoming.mapTo(HashSet()) { it.id }

        val removed = storedIds - incomingIds
        // Stay well under SQLite's bound-variable limit.
        removed.chunked(500).forEach { entryDao.deleteByIds(it) }

        val (existing, created) = incoming.partition { it.id in storedIds }
        if (existing.isNotEmpty()) entryDao.updateAll(existing)
        if (created.isNotEmpty()) entryDao.insertAll(created)
    }
}
