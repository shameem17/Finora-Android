package com.shameem.finora.app

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage { English, Bengali, System }

/** Observable, app-wide user settings (iOS `@Observable AppSettings`). */
@Stable
class AppSettings {
    var language: AppLanguage by mutableStateOf(AppLanguage.System)
    var notificationsEnabled: Boolean by mutableStateOf(true)
}
