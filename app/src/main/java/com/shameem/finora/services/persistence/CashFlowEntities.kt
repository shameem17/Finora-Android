package com.shameem.finora.services.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shameem.finora.screens.cashflow.model.CashFlowEntry
import com.shameem.finora.screens.categories.model.CategoryKind
import java.time.Instant
import java.util.UUID

@Entity(tableName = "cash_flow_entries", indices = [Index("kindKey")])
class CashFlowEntryEntity(
    @PrimaryKey val id: String,
    val kindKey: String,
    val categoryId: String,
    val accountId: String,
    val amount: Double,
    val date: Long,
    val note: String,
    val createdAt: Long,
) {
    fun toDomain(): CashFlowEntry = CashFlowEntry(
        id = UUID.fromString(id),
        kind = CategoryKind.fromStorageKey(kindKey) ?: CategoryKind.Expense,
        categoryId = UUID.fromString(categoryId),
        accountId = UUID.fromString(accountId),
        amount = amount,
        date = Instant.ofEpochMilli(date),
        note = note,
    )

    companion object {
        fun from(entry: CashFlowEntry, createdAt: Instant = Instant.now()) = CashFlowEntryEntity(
            id = entry.id.toString(),
            kindKey = entry.kind.storageKey,
            categoryId = entry.categoryId.toString(),
            accountId = entry.accountId.toString(),
            amount = entry.amount,
            date = entry.date.toEpochMilli(),
            note = entry.note,
            createdAt = createdAt.toEpochMilli(),
        )
    }
}

@Dao
interface CashFlowDao {
    @Query("SELECT * FROM cash_flow_entries WHERE kindKey = :kindKey ORDER BY createdAt")
    suspend fun byKind(kindKey: String): List<CashFlowEntryEntity>

    @Query("SELECT * FROM cash_flow_entries WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): CashFlowEntryEntity?

    @Insert
    suspend fun insert(entity: CashFlowEntryEntity)

    @Query(
        "UPDATE cash_flow_entries SET categoryId = :categoryId, accountId = :accountId, amount = :amount, " +
            "date = :date, note = :note WHERE id = :id",
    )
    suspend fun updateDetails(id: String, categoryId: String, accountId: String, amount: Double, date: Long, note: String)

    @Query("DELETE FROM cash_flow_entries WHERE id = :id")
    suspend fun delete(id: String)
}
