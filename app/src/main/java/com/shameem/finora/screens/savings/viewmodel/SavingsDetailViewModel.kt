package com.shameem.finora.screens.savings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.model.SavingsItem
import com.shameem.finora.screens.savings.service.SavingsDependencies
import com.shameem.finora.screens.transaction.AccountSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.UUID

data class SavingsDetailUiState(
    val item: SavingsItem? = null,
    val entries: List<SavingsEntry> = emptyList(),
    val accounts: List<Account> = emptyList(),
    /** False until the first load finishes, so a missing pot isn't mistaken for "not loaded yet". */
    val isLoaded: Boolean = false,
)

interface SavingsDetailViewModelProtocol {
    val state: StateFlow<SavingsDetailUiState>

    fun load()
    fun makeEntryFormViewModel(kind: SavingsEntryKind): SavingsEntryFormViewModel?
    fun makeEditEntryFormViewModel(entry: SavingsEntry): SavingsEntryFormViewModel?
    fun makeEditFormViewModel(): SavingsFormViewModel?
    fun makeLogViewModel(): SavingsLogViewModel

    /** Returns false if removing the entry would leave more withdrawn than saved. */
    suspend fun deleteEntry(id: UUID): Boolean
    fun deleteSavings(returnRemaining: Boolean)
}

class SavingsDetailViewModel(
    private val savingsId: UUID,
    private val dependencies: SavingsDependencies,
    /** Called after the pot is deleted so the coordinator can leave the detail screen. */
    private val onClose: () -> Unit,
) : ViewModel(), SavingsDetailViewModelProtocol {

    private val _state = MutableStateFlow(SavingsDetailUiState())
    override val state: StateFlow<SavingsDetailUiState> = _state.asStateFlow()

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
            val savings = dependencies.savings.savings(savingsId)
            val accounts = dependencies.accounts.fetchAccounts()
            _state.value = if (savings == null) {
                SavingsDetailUiState(accounts = accounts, isLoaded = true)
            } else {
                SavingsDetailUiState(
                    item = SavingsItem(savings, accounts.firstOrNull { it.id == savings.accountId }?.let(::AccountSummary)),
                    entries = savings.sortedEntries,
                    accounts = accounts,
                    isLoaded = true,
                )
            }
        }
    }

    override fun makeEntryFormViewModel(kind: SavingsEntryKind) =
        entryForm(SavingsEntryFormViewModel.Mode.Create(kind))

    override fun makeEditEntryFormViewModel(entry: SavingsEntry) =
        entryForm(SavingsEntryFormViewModel.Mode.Edit(entry))

    private fun entryForm(mode: SavingsEntryFormViewModel.Mode): SavingsEntryFormViewModel? {
        val current = _state.value
        val savings = current.item?.savings ?: return null
        return SavingsEntryFormViewModel(mode, savings, current.accounts, dependencies.service, viewModelScope)
    }

    override fun makeEditFormViewModel(): SavingsFormViewModel? {
        val current = _state.value
        val savings = current.item?.savings ?: return null
        return SavingsFormViewModel(
            mode = SavingsFormViewModel.Mode.Edit(savings),
            accounts = current.accounts,
            service = dependencies.service,
            scope = viewModelScope,
        )
    }

    override fun makeLogViewModel() = SavingsLogViewModel(savingsId, dependencies, viewModelScope)

    override suspend fun deleteEntry(id: UUID): Boolean = dependencies.service.deleteEntry(id, savingsId)

    override fun deleteSavings(returnRemaining: Boolean) {
        viewModelScope.launch {
            dependencies.service.delete(savingsId, returnRemaining)
            onClose()
        }
    }
}
