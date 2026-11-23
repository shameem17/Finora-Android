package com.shameem.finora.screens.loan.viewmodel

import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.service.LoanDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID

data class PersonRowData(
    val person: Person,
    /** Positive: they owe you; negative: you owe them. */
    val netBalance: Double,
    val activeCount: Int,
    val totalCount: Int,
) {
    val id: UUID get() = person.id
}

data class PeopleUiState(
    val searchText: String = "",
    val rows: List<PersonRowData> = emptyList(),
    /** True when nobody is saved at all (as opposed to nobody matching the search). */
    val isEmpty: Boolean = true,
)

interface PeopleViewModelProtocol {
    val state: StateFlow<PeopleUiState>
    fun setSearchText(text: String)
    suspend fun makeFormViewModel(editing: Person?): PersonFormViewModel
}

/** People list with their net balances. Listens to the repositories only while its screen is showing. */
@OptIn(ExperimentalCoroutinesApi::class)
class PeopleViewModel(
    private val dependencies: LoanDependencies,
    private val scope: CoroutineScope,
) : PeopleViewModelProtocol {

    private val query = MutableStateFlow("")

    private val allRows = merge(dependencies.loans.changes, dependencies.people.changes)
        .onStart { emit(Unit) }
        .mapLatest {
            val loansByPerson = dependencies.loans.fetchLoans().groupBy { it.personId }
            dependencies.people.fetchPeople().map { person ->
                val loans = loansByPerson[person.id].orEmpty()
                PersonRowData(
                    person = person,
                    netBalance = loans.sumOf { if (it.kind == LoanKind.Lent) it.outstanding else -it.outstanding },
                    activeCount = loans.count { !it.isSettled },
                    totalCount = loans.size,
                )
            }
        }

    override val state: StateFlow<PeopleUiState> = combine(allRows, query) { rows, text ->
        val needle = text.trim()
        PeopleUiState(
            searchText = text,
            rows = if (needle.isEmpty()) rows else rows.filter {
                it.person.name.contains(needle, ignoreCase = true) || (it.person.phone ?: "").contains(needle)
            },
            isEmpty = rows.isEmpty(),
        )
    }.stateIn(scope, SharingStarted.WhileSubscribed(5_000), PeopleUiState())

    override fun setSearchText(text: String) {
        query.update { text }
    }

    override suspend fun makeFormViewModel(editing: Person?): PersonFormViewModel {
        val existing = dependencies.people.fetchPeople()
        val linked = editing?.let { person -> dependencies.loans.fetchLoans().count { it.personId == person.id } } ?: 0
        return PersonFormViewModel(
            mode = editing?.let { PersonFormViewModel.Mode.Edit(it) } ?: PersonFormViewModel.Mode.Create,
            repository = dependencies.people,
            scope = scope,
            existingPeople = existing,
            linkedLoanCount = linked,
        )
    }
}
