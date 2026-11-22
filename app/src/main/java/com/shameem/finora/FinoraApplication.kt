package com.shameem.finora

import android.app.Application
import com.shameem.finora.app.AppContainer

class FinoraApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
