package com.shameem.finora.screens.loan.coordinator

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
import com.shameem.finora.screens.loan.view.LoanDetailView
import com.shameem.finora.screens.loan.view.LoanView
import com.shameem.finora.screens.loan.viewmodel.LoanDetailViewModel
import com.shameem.finora.screens.loan.viewmodel.LoanViewModel
import java.util.UUID

/** Loans flow: the list, and a detail screen for one entry that slides up like the iOS modal. */
class LoanCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        TabNavHost(startDestination = LIST) { navController ->
            composable(LIST) {
                val model = rememberViewModel { LoanViewModel(dependencies.loan) }
                LoanView(model = model, onShowDetail = { navController.navigate("$DETAIL/$it") })
            }
            composable(
                route = "$DETAIL/{$ARG_ID}",
                arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType }),
                enterTransition = { slideInVertically(initialOffsetY = { it }) },
                exitTransition = { null },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) },
            ) { entry ->
                val id = UUID.fromString(requireNotNull(entry.arguments?.getString(ARG_ID)))
                val model = rememberViewModel(key = "loan-detail-$id") {
                    LoanDetailViewModel(id, dependencies.loan, onClose = { navController.closeDetail() })
                }
                LoanDetailView(model = model, onBack = { navController.closeDetail() })
            }
        }
    }

    /** Idempotent: deleting an entry triggers this from two places and must pop only once. */
    private fun NavHostController.closeDetail() {
        if (currentDestination?.route == "$DETAIL/{$ARG_ID}") popBackStack()
    }

    private companion object {
        const val LIST = "loans"
        const val DETAIL = "loans/detail"
        const val ARG_ID = "id"
    }
}
