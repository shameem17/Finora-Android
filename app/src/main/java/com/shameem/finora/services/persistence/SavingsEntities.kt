package com.shameem.finora.services.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import java.time.Instant
import java.util.UUID

@Entity(tableName = "savings")
class SavingsEntity(
    @PrimaryKey val id: String,
    val name: String,
    val number: String,
    val accountId: String,
    val logoData: ByteArray?,
    val tint: String,
    val startDate: Long,
    val target: Double?,
    val targetDate: Long?,
    val note: String,
    val openingBalance: Double,
    val createdAt: Long,
) {
    fun toDomain(entries: List<SavingsEntry>): SavingsAccount = SavingsAccount(
        id = UUID.fromString(id),
        name = name,
        number = number,
        accountId = UUID.fromString(accountId),
        logoData = logoData,
        tint = tint,
        startDate = Instant.ofEpochMilli(startDate),
        target = target,
        targetDate = targetDate?.let(Instant::ofEpochMilli),
        note = note,
        openingBalance = openingBalance,
        entries = entries,
    )

    companion object {
        fun from(savings: SavingsAccount, createdAt: Instant = Instant.now()) = SavingsEntity(
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
            createdAt = createdAt.toEpochMilli(),
        )
    }
}

/** Entries are owned by their pot: deleting the pot cascades to them. */
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
class SavingsEntryEntity(
    @PrimaryKey val id: String,
    val savingsId: String,
    val kindKey: String,
    val amount: Double,
    val date: Long,
    val note: String,
    val accountJson: String?,
    /** Position in the pot's entry list, so insertion order survives a round trip. */
    val position: Int,
) {
    fun toDomain(): SavingsEntry = SavingsEntry(
        id = UUID.fromString(id),
        kind = SavingsEntryKind.fromStorageKey(kindKey) ?: SavingsEntryKind.Deposit,
        amount = amount,
        date = Instant.ofEpochMilli(date),
        note = note,
        account = accountJson?.let { AccountSnapshot.fromJson(it).summary },
    )

    companion object {
        fun from(entry: SavingsEntry, savingsId: UUID, position: Int) = SavingsEntryEntity(
            id = entry.id.toString(),
            savingsId = savingsId.toString(),
            kindKey = entry.kind.storageKey,
            amount = entry.amount,
            date = entry.date.toEpochMilli(),
            note = entry.note,
            accountJson = entry.account?.let { AccountSnapshot(it).toJson() },
            position = position,
        )
    }
}

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings ORDER BY createdAt")
    suspend fun all(): List<SavingsEntity>

    @Query("SELECT * FROM savings WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): SavingsEntity?

    @Query("SELECT * FROM savings_entries ORDER BY position")
    suspend fun allEntries(): List<SavingsEntryEntity>

    @Query("SELECT * FROM savings_entries WHERE savingsId = :savingsId ORDER BY position")
    suspend fun entriesOf(savingsId: String): List<SavingsEntryEntity>

    @Insert
    suspend fun insert(entity: SavingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEntries(entries: List<SavingsEntryEntity>)

    @Query("DELETE FROM savings_entries WHERE savingsId = :savingsId")
    suspend fun deleteEntriesOf(savingsId: String)

    @Query(
        "UPDATE savings SET name = :name, number = :number, accountId = :accountId, logoData = :logoData, " +
            "tint = :tint, startDate = :startDate, target = :target, targetDate = :targetDate, note = :note, " +
            "openingBalance = :openingBalance WHERE id = :id",
    )
    suspend fun updateDetails(
        id: String,
        name: String,
        number: String,
        accountId: String,
        logoData: ByteArray?,
        tint: String,
        startDate: Long,
        target: Double?,
        targetDate: Long?,
        note: String,
        openingBalance: Double,
    )

    @Query("DELETE FROM savings WHERE id = :id")
    suspend fun delete(id: String)
}
