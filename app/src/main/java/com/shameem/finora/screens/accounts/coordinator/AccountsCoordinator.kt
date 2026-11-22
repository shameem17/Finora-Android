package com.shameem.finora.screens.accounts.coordinator

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.accounts.view.AccountsView
import com.shameem.finora.screens.accounts.viewmodel.AccountsViewModel

class AccountsCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        val viewModel = rememberViewModel { AccountsViewModel(dependencies.accountRepository) }
        AccountsView(model = viewModel)
    }
}
