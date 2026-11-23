package com.shameem.finora.screens.savings.coordinator

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.TabNavHost
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.savings.view.SavingsDetailView
import com.shameem.finora.screens.savings.view.SavingsView
import com.shameem.finora.screens.savings.viewmodel.SavingsDetailViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsViewModel
import java.util.UUID

/**
 * Savings flow: the list, and a detail screen for one pot that slides up like the iOS modal
 * (iOS `SavingsCoordinator` + `SavingsRouting`).
 */
class SavingsCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        TabNavHost(startDestination = LIST) { navController ->
            composable(LIST) {
                val model = rememberViewModel { SavingsViewModel(dependencies.savings) }
                SavingsView(
                    model = model,
                    onShowDetail = { navController.navigate("$DETAIL/$it") },
                )
            }
            composable(
                route = "$DETAIL/{$ARG_ID}",
                arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType }),
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { null },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) },
            ) { entry ->
                val id = UUID.fromString(requireNotNull(entry.arguments?.getString(ARG_ID)))
                val model = rememberViewModel(key = "savings-detail-$id") {
                    SavingsDetailViewModel(id, dependencies.savings, onClose = { navController.closeDetail() })
                }
                SavingsDetailView(model = model, onBack = { navController.closeDetail() })
            }
        }
    }

    /** Idempotent: deleting a pot triggers this from two places and must pop only once. */
    private fun NavHostController.closeDetail() {
        if (currentDestination?.route == "$DETAIL/{$ARG_ID}") popBackStack()
    }

    private companion object {
        const val LIST = "savings"
        const val DETAIL = "savings/detail"
        const val ARG_ID = "id"
    }
}
