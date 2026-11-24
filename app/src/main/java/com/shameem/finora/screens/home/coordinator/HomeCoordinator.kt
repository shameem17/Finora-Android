package com.shameem.finora.screens.home.coordinator

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.TabNavHost
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.home.view.HomeView
import com.shameem.finora.screens.home.viewmodel.HomeViewModel

/** Home is a single screen, so its flow is just the start destination (iOS `HomeCoordinator`). */
class HomeCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        TabNavHost(startDestination = HOME) { _ ->
            androidx.navigation.compose.composable(HOME) {
                val model = rememberViewModel {
                    HomeViewModel(
                        dateService = dependencies.dateService,
                        accountRepository = dependencies.accountRepository,
                        cashFlowRepository = dependencies.cashFlow.entries,
                        categoryRepository = dependencies.categoryRepository,
                        loanRepository = dependencies.loan.loans,
                        savingsRepository = dependencies.savings.savings,
                    )
                }
                HomeView(model)
            }
        }
    }

    private companion object {
        const val HOME = "home"
    }
}
