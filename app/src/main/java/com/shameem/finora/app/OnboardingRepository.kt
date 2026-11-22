package com.shameem.finora.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

/** Persists whether the user has completed onboarding. */
interface OnboardingRepository {
    /** `null` until the persisted value has been read (keep the splash screen up until then). */
    val hasOnboarded: StateFlow<Boolean?>

    suspend fun markOnboarded()
}

class DataStoreOnboardingRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) : OnboardingRepository {

    override val hasOnboarded: StateFlow<Boolean?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[ONBOARDING_KEY] ?: false }
        .stateIn(scope, SharingStarted.Eagerly, initialValue = null)

    override suspend fun markOnboarded() {
        dataStore.edit { preferences -> preferences[ONBOARDING_KEY] = true }
    }

    private companion object {
        val ONBOARDING_KEY = booleanPreferencesKey("onboarding_status_finora")
    }
}
