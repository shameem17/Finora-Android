package com.shameem.finora.services.persistence

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.shameem.finora.screens.categories.model.TransactionCategory
import java.time.Instant

/**
 * Seeds the default categories exactly once, so a user who deletes them is never re-seeded
 * (iOS `DefaultDataSeeder`).
 */
class DefaultDataSeeder(
    private val database: FinoraDatabase,
    private val preferences: SharedPreferences,
    /** Tells observers the stored categories changed (the seed bypasses the repository). */
    private val onSeeded: () -> Unit = {},
) {
    suspend fun seedIfNeeded() {
        if (preferences.getBoolean(KEY, false)) return

        val dao = database.categoryDao()
        database.withTransaction {
            if (dao.count() == 0) {
                // Distinct timestamps keep the defaults in their declared order (lists sort by creation time).
                val now = Instant.now()
                dao.insertAll(
                    TransactionCategory.defaults.mapIndexed { index, category ->
                        CategoryEntity.from(category, createdAt = now.plusMillis(index.toLong()))
                    },
                )
            }
        }
        if (dao.count() > 0) {
            preferences.edit().putBoolean(KEY, true).apply()
            onSeeded()
        }
    }

    private companion object {
        const val KEY = "finora.seed.categories.v1"
    }
}
