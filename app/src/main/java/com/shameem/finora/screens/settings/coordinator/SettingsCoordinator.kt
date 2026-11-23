package com.shameem.finora.screens.settings.coordinator

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.screens.settings.view.SettingsView

class SettingsCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        SettingsView(settings = dependencies.settings)
    }
}
