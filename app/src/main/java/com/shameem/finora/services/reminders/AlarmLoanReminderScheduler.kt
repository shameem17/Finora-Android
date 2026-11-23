package com.shameem.finora.services.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.service.LoanReminderScheduling
import java.time.Clock
import java.time.LocalTime
import java.util.UUID

/**
 * Schedules a local notification at 09:00 on the day a loan's next instalment is due (iOS
 * `LoanReminderScheduler`). Uses an inexact, doze-friendly alarm, so no exact-alarm permission is needed;
 * the reminder may arrive a little late but never early.
 */
class AlarmLoanReminderScheduler(
    context: Context,
    private val isEnabled: () -> Boolean = { true },
    private val clock: Clock = Clock.systemDefaultZone(),
) : LoanReminderScheduling {

    private val context = context.applicationContext
    private val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun sync(loan: Loan, person: Person?) {
        cancel(loan.id)
        if (!isEnabled() || loan.isSettled) return

        val zone = clock.zone
        val next = loan.nextInstallment(zone) ?: return
        val fireAt = next.dueDate.atZone(zone).toLocalDate().atTime(REMINDER_TIME).atZone(zone).toInstant()
        if (!fireAt.isAfter(clock.instant())) return

        val name = person?.name ?: "someone"
        val amount = CurrencyFormatter.taka(next.remaining, showFraction = true)
        val (title, body) = when (loan.kind) {
            LoanKind.Borrowed -> "Payment due today" to "Pay $amount to $name."
            LoanKind.Lent -> "Repayment due today" to "$name is due to repay $amount."
        }

        val intent = intentFor(loan.id).putExtra(LoanReminderReceiver.EXTRA_TITLE, title)
            .putExtra(LoanReminderReceiver.EXTRA_BODY, body)
        val pending = PendingIntent.getBroadcast(context, 0, intent, FLAGS_UPDATE)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAt.toEpochMilli(), pending)
    }

    override fun cancel(loanId: UUID) {
        val existing = PendingIntent.getBroadcast(context, 0, intentFor(loanId), FLAGS_NO_CREATE) ?: return
        alarmManager.cancel(existing)
        existing.cancel()
    }

    /** Identified by data URI, so each loan has exactly one pending alarm. */
    private fun intentFor(loanId: UUID) =
        Intent(context, LoanReminderReceiver::class.java).setData(Uri.parse("finora://loan/$loanId"))

    private companion object {
        val REMINDER_TIME: LocalTime = LocalTime.of(9, 0)
        const val FLAGS_UPDATE = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        const val FLAGS_NO_CREATE = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    }
}
