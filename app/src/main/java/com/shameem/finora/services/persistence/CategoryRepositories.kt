package com.shameem.finora.services.persistence

import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.categories.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RoomCategoryRepository(database: FinoraDatabase) : CategoryRepository {

    private val dao = database.categoryDao()
    private val signal = ChangeSignal()

    override val changes: Flow<Unit> get() = signal.changes

    /** Announces a change made outside this repository (first-launch seeding). */
    fun invalidate() = signal.emitNow()

    override suspend fun fetchCategories(kind: CategoryKind): List<TransactionCategory> =
        dao.byKind(kind.storageKey).map(CategoryEntity::toDomain)

    override suspend fun fetchAll(): List<TransactionCategory> = dao.all().map(CategoryEntity::toDomain)

    override suspend fun add(category: TransactionCategory) {
        dao.insert(CategoryEntity.from(category))
        signal.notifyChanged()
    }

    override suspend fun update(category: TransactionCategory) {
        dao.updateDetails(category.id.toString(), category.name, category.icon, category.tint, category.monthlyBudget)
        signal.notifyChanged()
    }

    override suspend fun delete(id: UUID) {
        dao.delete(id.toString())
        signal.notifyChanged()
    }
}
