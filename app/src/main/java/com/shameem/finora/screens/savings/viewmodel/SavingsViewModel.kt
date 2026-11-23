package com.shameem.finora.screens.savings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.model.SavingsItem
import com.shameem.finora.screens.savings.model.SavingsSummary
import com.shameem.finora.screens.savings.service.SavingsDependencies
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.UUID

data class SavingsUiState(
    val items: List<SavingsItem> = emptyList(),
    val summary: SavingsSummary = SavingsSummary.Empty,
    val accounts: List<Account> = emptyList(),
)

interface SavingsViewModelProtocol {
    val state: StateFlow<SavingsUiState>

    fun load()
    fun makeLogViewModel(): SavingsLogViewModel
    fun makeFormViewModel(): SavingsFormViewModel
    fun makeEntryFormViewModel(id: UUID, kind: SavingsEntryKind): SavingsEntryFormViewModel?
}

class SavingsViewModel(
    private val dependencies: SavingsDependencies,
) : ViewModel(), SavingsViewModelProtocol {

    private val _state = MutableStateFlow(SavingsUiState())
    override val state: StateFlow<SavingsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            merge(dependencies.savings.changes, dependencies.accounts.changes).collect { load() }
        }
        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val accounts = dependencies.accounts.fetchAccounts()
            val savings = dependencies.savings.fetchSavings()
            val summaries = accounts.associate { it.id to AccountSummary(it) }
            _state.value = SavingsUiState(
                items = savings.sortedByDescending { it.currentSavings }.map { SavingsItem(it, summaries[it.accountId]) },
                summary = SavingsSummary.make(savings, accounts),
                accounts = accounts,
            )
        }
    }

    override fun makeLogViewModel() = SavingsLogViewModel(scopeId = null, dependencies = dependencies, scope = viewModelScope)

    override fun makeFormViewModel() = SavingsFormViewModel(
        mode = SavingsFormViewModel.Mode.Create,
        accounts = _state.value.accounts,
        service = dependencies.service,
        scope = viewModelScope,
    )

    override fun makeEntryFormViewModel(id: UUID, kind: SavingsEntryKind): SavingsEntryFormViewModel? {
        val current = _state.value
        val savings = current.items.firstOrNull { it.id == id }?.savings ?: return null
        return SavingsEntryFormViewModel(
            mode = SavingsEntryFormViewModel.Mode.Create(kind),
            savings = savings,
            accounts = current.accounts,
            service = dependencies.service,
            scope = viewModelScope,
        )
    }
}
