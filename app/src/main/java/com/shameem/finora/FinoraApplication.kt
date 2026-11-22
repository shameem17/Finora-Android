package com.shameem.finora

import android.app.Application
import com.shameem.finora.app.AppContainer
import kotlinx.coroutines.launch

class FinoraApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).also { c ->
            // First launch only: default categories. Runs off the main thread; the UI reads via change signals.
            c.applicationScope.launch { c.dataSeeder.seedIfNeeded() }
        }
    }
}
