package com.shameem.finora.services.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The app's single on-device database (iOS SwiftData `ModelContainer`). Everything is stored locally;
 * nothing is ever transmitted.
 */
@Database(
    entities = [
        AccountEntity::class,
        TransferEntity::class,
        CategoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FinoraDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun categoryDao(): CategoryDao
}
