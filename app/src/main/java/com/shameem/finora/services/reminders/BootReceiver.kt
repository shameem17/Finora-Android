package com.shameem.finora.services.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shameem.finora.FinoraApplication
import kotlinx.coroutines.launch

/** Alarms are wiped on reboot (and app update); rebuild every loan's reminder from the database. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val container = (context.applicationContext as FinoraApplication).container
        val pending = goAsync()
        container.applicationScope.launch {
            try {
                container.resyncLoanReminders()
            } finally {
                pending.finish()
            }
        }
    }
}
