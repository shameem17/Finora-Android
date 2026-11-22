package com.shameem.finora.app

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.shameem.finora.core.data.RoomUnitOfWork
import com.shameem.finora.core.data.UnitOfWork
import com.shameem.finora.screens.transaction.AccountRepository
import com.shameem.finora.screens.transaction.TransferRepository
import com.shameem.finora.services.date.DateService
import com.shameem.finora.services.date.SystemDateService
import com.shameem.finora.services.persistence.FinoraDatabase
import com.shameem.finora.services.persistence.RoomAccountRepository
import com.shameem.finora.services.persistence.RoomTransferRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

private val Context.finoraDataStore by preferencesDataStore(name = "finora_preferences")

/** Manual dependency container; lives as long as the process, so it survives configuration changes. */
class AppContainer(context: Context) : AppDependencies {

    override val appContext: Context = context.applicationContext

    override val settings: AppSettings = AppSettings(
        appContext.getSharedPreferences(AppSettings.PREFERENCES_NAME, Context.MODE_PRIVATE),
    )

    override val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val onboardingRepository: OnboardingRepository =
        DataStoreOnboardingRepository(appContext.finoraDataStore, applicationScope)

    override val database: FinoraDatabase =
        Room.databaseBuilder(appContext, FinoraDatabase::class.java, "finora.db").build()

    override val unitOfWork: UnitOfWork = RoomUnitOfWork(database)

    override val dateService: DateService = SystemDateService()

    override val accountRepository: AccountRepository = RoomAccountRepository(database)

    override val transferRepository: TransferRepository = RoomTransferRepository(database)
}
