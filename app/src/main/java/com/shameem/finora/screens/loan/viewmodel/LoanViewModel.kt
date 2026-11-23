package com.shameem.finora.screens.loan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanItem
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.LoanSection
import com.shameem.finora.screens.loan.model.LoanStatus
import com.shameem.finora.screens.loan.model.LoanSummary
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.service.LoanDependencies
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.UUID

data class LoanUiState(
    val period: ReportPeriod = ReportPeriod.Month,
    val kind: LoanKind = LoanKind.Lent,
    val searchText: String = "",
    val summary: LoanSummary = LoanSummary.Empty,
    val sections: List<LoanSection> = emptyList(),
    /** True when there are no loans of the selected kind at all (as opposed to none matching the search). */
    val isEmpty: Boolean = true,
)

interface LoanViewModelProtocol {
    val state: StateFlow<LoanUiState>

    fun setPeriod(period: ReportPeriod)
    fun setKind(kind: LoanKind)
    fun setSearchText(text: String)
    fun load()

    fun makeFormViewModel(): LoanFormViewModel
    fun makePeopleViewModel(): PeopleViewModel
    suspend fun makePersonFormViewModel(): PersonFormViewModel
}

class LoanViewModel(
    private val dependencies: LoanDependencies,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel(), LoanViewModelProtocol {

    private val _state = MutableStateFlow(LoanUiState())
    override val state: StateFlow<LoanUiState> = _state.asStateFlow()

    private var allLoans: List<Loan> = emptyList()
    private var peopleById: Map<UUID, Person> = emptyMap()
    private var accounts: List<Account> = emptyList()
    private var people: List<Person> = emptyList()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            merge(dependencies.loans.changes, dependencies.people.changes, dependencies.accounts.changes).collect { load() }
        }
        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            allLoans = dependencies.loans.fetchLoans()
            people = dependencies.people.fetchPeople()
            peopleById = people.associateBy { it.id }
            accounts = dependencies.accounts.fetchAccounts()
            publish()
        }
    }

    override fun setPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period)
        publish()
    }

    override fun setKind(kind: LoanKind) {
        _state.value = _state.value.copy(kind = kind)
        publish()
    }

    override fun setSearchText(text: String) {
        _state.value = _state.value.copy(searchText = text)
        publish()
    }

    override fun makeFormViewModel() = LoanFormViewModel(
        mode = LoanFormViewModel.Mode.Create(_state.value.kind),
        accounts = accounts,
        people = people,
        dependencies = dependencies,
        scope = viewModelScope,
    )

    override fun makePeopleViewModel() = PeopleViewModel(dependencies, viewModelScope)

    override suspend fun makePersonFormViewModel() = PersonFormViewModel(
        mode = PersonFormViewModel.Mode.Create,
        repository = dependencies.people,
        scope = viewModelScope,
        existingPeople = dependencies.people.fetchPeople(),
    )

    private fun publish() {
        val current = _state.value
        val ofKind = allLoans.filter { it.kind == current.kind }
        val needle = current.searchText.trim()

        val items = ofKind
            .map { LoanItem.of(it, peopleById[it.personId], clock) }
            .filter { item ->
                needle.isEmpty() ||
                    item.person.name.contains(needle, ignoreCase = true) ||
                    (item.loan.note ?: "").contains(needle, ignoreCase = true)
            }

        val overdue = items.filter { it.status is LoanStatus.Overdue }
            .sortedByDescending { it.status.overdueDays ?: 0 }
        val settled = items.filter { it.status == LoanStatus.Settled }
            .sortedByDescending { it.loan.lastActivity }
        // Soonest due first; open loans last; ties broken by newest loan.
        val active = items.filter { it.status is LoanStatus.Due || it.status == LoanStatus.Open }
            .sortedWith(compareBy<LoanItem> { it.status.dueRank }.thenByDescending { it.loan.date })

        _state.value = current.copy(
            summary = LoanSummary.make(allLoans, current.period, clock),
            sections = listOf(
                LoanSection(LoanSection.Group.Overdue, overdue),
                LoanSection(LoanSection.Group.Active, active),
                LoanSection(LoanSection.Group.Settled, settled),
            ).filter { it.items.isNotEmpty() },
            isEmpty = ofKind.isEmpty(),
        )
    }
}
