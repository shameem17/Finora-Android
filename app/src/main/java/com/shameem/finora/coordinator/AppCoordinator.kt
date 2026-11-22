package com.shameem.finora.coordinator

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.screens.onboarding.OnboardingCoordinator
import kotlinx.coroutines.launch

/**
 * Root coordinator. Shows onboarding on first launch and the main tab flow afterwards.
 * (iOS `AppCoordinator`.) Onboarding completion is persisted, and the persisted value drives
 * the switch, so there is a single source of truth.
 */
class AppCoordinator(private val dependencies: AppDependencies) : Coordinator {

    private val onboardingCoordinator = OnboardingCoordinator(
        onFinished = {
            dependencies.applicationScope.launch {
                dependencies.onboardingRepository.markOnboarded()
            }
        },
    )

    private val mainCoordinator by lazy { MainCoordinator(dependencies) }

    @Composable
    override fun Start() {
        val hasOnboarded by dependencies.onboardingRepository.hasOnboarded.collectAsStateWithLifecycle()

        Crossfade(
            targetState = hasOnboarded,
            animationSpec = tween(durationMillis = 400),
            label = "root-flow",
        ) { onboarded ->
            when (onboarded) {
                // Still reading the persisted flag; the system splash screen covers this.
                null -> Box(Modifier.fillMaxSize().background(FinoraColors.Background))
                false -> onboardingCoordinator.Start()
                true -> mainCoordinator.Start()
            }
        }
    }
}
