package com.shameem.finora.services.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import java.time.Instant
import java.util.UUID

@Entity(tableName = "categories")
class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val kindKey: String,
    val icon: String,
    val tint: String,
    val monthlyBudget: Double?,
    val createdAt: Long,
) {
    fun toDomain(): TransactionCategory = TransactionCategory(
        id = UUID.fromString(id),
        name = name,
        kind = CategoryKind.fromStorageKey(kindKey) ?: CategoryKind.Expense,
        icon = icon,
        tint = tint,
        monthlyBudget = monthlyBudget,
    ).normalized()

    companion object {
        fun from(category: TransactionCategory, createdAt: Instant = Instant.now()) = CategoryEntity(
            id = category.id.toString(),
            name = category.name,
            kindKey = category.kind.storageKey,
            icon = category.icon,
            tint = category.tint,
            monthlyBudget = category.monthlyBudget,
            createdAt = createdAt.toEpochMilli(),
        )
    }
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE kindKey = :kindKey ORDER BY createdAt")
    suspend fun byKind(kindKey: String): List<CategoryEntity>

    @Query("SELECT * FROM categories ORDER BY createdAt")
    suspend fun all(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert
    suspend fun insert(entity: CategoryEntity)

    @Insert
    suspend fun insertAll(entities: List<CategoryEntity>)

    @Query("UPDATE categories SET name = :name, icon = :icon, tint = :tint, monthlyBudget = :budget WHERE id = :id")
    suspend fun updateDetails(id: String, name: String, icon: String, tint: String, budget: Double?)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}
