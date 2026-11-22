package com.shameem.finora.screens.onboarding

import androidx.compose.runtime.Composable
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.onboarding.view.OnboardingScreen

/** Runs the first-launch onboarding flow and reports completion through [onFinished]. */
class OnboardingCoordinator(
    private val onFinished: () -> Unit,
) : Coordinator {

    @Composable
    override fun Start() {
        val viewModel = rememberViewModel { OnboardingViewModel(onFinished) }
        OnboardingScreen(model = viewModel)
    }
}
