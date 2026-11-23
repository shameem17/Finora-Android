package com.shameem.finora.screens.savings.viewmodel

import com.shameem.finora.screens.savings.model.SavingsLogEntry
import com.shameem.finora.screens.savings.model.SavingsLogFilter
import com.shameem.finora.screens.savings.model.SavingsLogSection
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.service.SavingsDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SavingsLogUiState(
    val filter: SavingsLogFilter = SavingsLogFilter.All,
    /** The pot's name when the log is scoped to one pot, else null. */
    val scopeTitle: String? = null,
    val sections: List<SavingsLogSection> = emptyList(),
    val totalAdded: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    /** True when there is no activity at all (as opposed to everything being filtered out). */
    val isEmpty: Boolean = true,
)

interface SavingsLogViewModelProtocol {
    val state: StateFlow<SavingsLogUiState>
    fun setFilter(filter: SavingsLogFilter)
}

/**
 * History of deposits and withdrawals, for one pot ([scopeId]) or all of them. It only listens to the
 * repository while its screen is showing, so opening many logs never leaves collectors behind.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SavingsLogViewModel(
    private val scopeId: UUID?,
    dependencies: SavingsDependencies,
    scope: CoroutineScope,
) : SavingsLogViewModelProtocol {

    private val filter = MutableStateFlow(SavingsLogFilter.All)

    private class Source(val scopeTitle: String?, val entries: List<SavingsLogEntry>)

    private val source = dependencies.savings.changes
        .onStart { emit(Unit) }
        .mapLatest {
            var pots = dependencies.savings.fetchSavings()
            if (scopeId != null) pots = pots.filter { it.id == scopeId }
            Source(
                scopeTitle = if (scopeId == null) null else pots.firstOrNull()?.name,
                entries = pots.flatMap { pot -> pot.entries.map { SavingsLogEntry(it, pot.id, pot.name) } },
            )
        }

    override val state: StateFlow<SavingsLogUiState> = combine(source, filter) { source, filter ->
        val visible = source.entries.filter { filter.includes(it.entry.kind) }
        SavingsLogUiState(
            filter = filter,
            scopeTitle = source.scopeTitle,
            sections = SavingsLogSection.make(visible),
            totalAdded = visible.filter { it.entry.kind == SavingsEntryKind.Deposit }.sumOf { it.entry.amount },
            totalWithdrawn = visible.filter { it.entry.kind == SavingsEntryKind.Withdrawal }.sumOf { it.entry.amount },
            isEmpty = source.entries.isEmpty(),
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), SavingsLogUiState())

    override fun setFilter(filter: SavingsLogFilter) {
        this.filter.update { filter }
    }
}
