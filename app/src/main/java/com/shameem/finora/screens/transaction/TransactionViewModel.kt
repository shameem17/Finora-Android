package com.shameem.finora.screens.transaction

import com.shameem.finora.core.util.ReportPeriod
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.core.data.ImmediateUnitOfWork
import com.shameem.finora.core.data.UnitOfWork
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

/** Everything the Transactions screen renders. */
data class TransactionUiState(
    val period: ReportPeriod = ReportPeriod.Month,
    val summary: TransferSummary = TransferSummary.Empty,
    val sections: List<TransferSection> = emptyList(),
)

interface TransactionViewModelProtocol {
    val state: StateFlow<TransactionUiState>

    fun setPeriod(period: ReportPeriod)
    fun load()

    /** Builds a blank form pre-filled with the current accounts. */
    suspend fun makeFormViewModel(): TransferFormViewModel
}

class TransactionViewModel(
    private val accountRepository: AccountRepository,
    private val transferRepository: TransferRepository,
    private val unitOfWork: UnitOfWork = ImmediateUnitOfWork,
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val locale: Locale = Locale.getDefault(),
) : ViewModel(), TransactionViewModelProtocol {

    private val _state = MutableStateFlow(TransactionUiState())
    override val state: StateFlow<TransactionUiState> = _state.asStateFlow()

    private var allTransfers: List<Transfer> = emptyList()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { transferRepository.changes.collect { load() } }
        load()
    }

    override fun setPeriod(period: ReportPeriod) {
        _state.update { it.copy(period = period) }
        rebuild()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            allTransfers = transferRepository.fetchTransfers()
            rebuild()
        }
    }

    override suspend fun makeFormViewModel(): TransferFormViewModel =
        TransferFormViewModel(accounts = accountRepository.fetchAccounts()) { transfer -> record(transfer) }

    /** Saves the transfer and moves the money in one atomic step; silently ignores an invalid one. */
    private fun record(transfer: Transfer) {
        viewModelScope.launch {
            unitOfWork.perform {
                val source = accountRepository.account(transfer.from.id) ?: return@perform
                if (accountRepository.account(transfer.to.id) == null) return@perform
                if (source.balance + 0.005 < transfer.totalDebited) return@perform
                transferRepository.add(transfer)
                accountRepository.apply(transfer)
            }
        }
    }

    private fun rebuild() {
        val period = _state.value.period
        val today = LocalDate.now(zone)
        val visible = allTransfers.filter { period.contains(it.date, zone, today, locale) }

        val summary = TransferSummary(
            totalAmount = visible.sumOf { it.amount },
            totalCharges = visible.sumOf { it.charge },
            count = visible.size,
        )
        val sections = visible
            .groupBy { it.date.atZone(zone).toLocalDate() }
            .map { (day, items) ->
                TransferSection(
                    id = day.atStartOfDay(zone).toInstant(),
                    title = title(day, today),
                    transfers = items.sortedByDescending { it.date },
                )
            }
            .sortedByDescending { it.id }

        _state.update { it.copy(summary = summary, sections = sections) }
    }

    private fun title(day: LocalDate, today: LocalDate): String = when (day) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> "${day.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}, " +
            "${day.dayOfMonth} ${day.month.getDisplayName(TextStyle.SHORT, locale)}"
    }
}
