package com.shameem.finora.core.data

import kotlinx.coroutines.flow.Flow

/**
 * Anything whose stored data can change underneath its observers (iOS `ChangeNotifying`).
 * [changes] emits once after each committed write so view models re-read stored truth.
 */
interface ChangeNotifying {
    val changes: Flow<Unit>
}
