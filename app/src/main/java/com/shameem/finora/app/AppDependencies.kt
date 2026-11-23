package com.shameem.finora.app

import android.content.Context
import com.shameem.finora.core.data.UnitOfWork
import com.shameem.finora.screens.cashflow.service.CashFlowDependencies
import com.shameem.finora.screens.loan.service.LoanDependencies
import com.shameem.finora.screens.savings.service.SavingsDependencies
import com.shameem.finora.screens.categories.repository.CategoryRepository
import com.shameem.finora.screens.transaction.AccountRepository
import com.shameem.finora.screens.transaction.TransferRepository
import com.shameem.finora.services.date.DateService
import com.shameem.finora.services.persistence.FinoraDatabase
import kotlinx.coroutines.CoroutineScope

/** What a coordinator may ask for. Coordinators depend on this interface, never on the concrete container. */
interface AppDependencies {
    val appContext: Context
    val settings: AppSettings
    val applicationScope: CoroutineScope
    val onboardingRepository: OnboardingRepository
    val database: FinoraDatabase
    val unitOfWork: UnitOfWork
    val dateService: DateService

    // Shared singletons: several features read and move money through these.
    val accountRepository: AccountRepository
    val transferRepository: TransferRepository
    val categoryRepository: CategoryRepository
    val cashFlow: CashFlowDependencies
    val savings: SavingsDependencies
    val loan: LoanDependencies
}
