package com.shameem.finora.screens.categories.repository

import com.shameem.finora.core.data.ChangeNotifying
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import java.util.UUID

interface CategoryRepository : ChangeNotifying {
    /** Categories of [kind], oldest first. */
    suspend fun fetchCategories(kind: CategoryKind): List<TransactionCategory>

    /** Every category of both kinds (used to resolve names for entries). */
    suspend fun fetchAll(): List<TransactionCategory>

    suspend fun add(category: TransactionCategory)
    suspend fun update(category: TransactionCategory)
    suspend fun delete(id: UUID)
}
