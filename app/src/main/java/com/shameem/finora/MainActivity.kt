package com.shameem.finora

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shameem.finora.coordinator.AppCoordinator
import com.shameem.finora.core.designsystem.FinoraTheme

class MainActivity : ComponentActivity() {

    private val appCoordinator by lazy {
        AppCoordinator((application as FinoraApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val container = (application as FinoraApplication).container
        val splash = installSplashScreen()
        // Hold the splash until the persisted onboarding flag has been read, so the right flow shows first.
        splash.setKeepOnScreenCondition { container.onboardingRepository.hasOnboarded.value == null }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            FinoraTheme { appCoordinator.Start() }
        }
    }
}
