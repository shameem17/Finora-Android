package com.shameem.finora.app

import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.util.AppCurrency

/** Observable, app-wide user settings (iOS `@Observable AppSettings`). */
@Stable
class AppSettings(private val preferences: SharedPreferences) {

    var currency: AppCurrency by mutableStateOf(AppCurrency.current)
        private set

    fun updateCurrency(value: AppCurrency) {
        currency = value
        AppCurrency.current = value
    }

    /** Whether loan reminders may post notifications. Persisted across launches. */
    var notificationsEnabled: Boolean by mutableStateOf(preferences.getBoolean(KEY_NOTIFICATIONS, true))
        private set

    fun updateNotificationsEnabled(enabled: Boolean) {
        notificationsEnabled = enabled
        preferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
    }

    companion object {
        const val PREFERENCES_NAME = "finora_settings"
        const val KEY_NOTIFICATIONS = "finora.settings.notificationsEnabled"
    }
}
