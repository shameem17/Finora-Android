package com.shameem.finora.screens.transaction

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.rememberViewModel

class TransactionCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        val viewModel = rememberViewModel {
            TransactionViewModel(
                accountRepository = dependencies.accountRepository,
                transferRepository = dependencies.transferRepository,
                unitOfWork = dependencies.unitOfWork,
            )
        }
        TransactionView(model = viewModel)
    }
}
