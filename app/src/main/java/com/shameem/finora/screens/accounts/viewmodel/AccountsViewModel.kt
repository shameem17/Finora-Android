package com.shameem.finora.screens.accounts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.transaction.AccountRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
) {
    val totalBalance: Double get() = accounts.sumOf { it.balance }
}

interface AccountsViewModelProtocol {
    val state: StateFlow<AccountsUiState>

    fun load()
    fun makeFormViewModel(editing: Account?): AccountFormViewModel
}

class AccountsViewModel(
    private val repository: AccountRepository,
) : ViewModel(), AccountsViewModelProtocol {

    private val _state = MutableStateFlow(AccountsUiState())
    override val state: StateFlow<AccountsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch { repository.changes.collect { load() } }
        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch { _state.value = AccountsUiState(repository.fetchAccounts()) }
    }

    override fun makeFormViewModel(editing: Account?): AccountFormViewModel =
        AccountFormViewModel(
            mode = editing?.let { AccountFormViewModel.Mode.Edit(it) } ?: AccountFormViewModel.Mode.Create,
            repository = repository,
            scope = viewModelScope,
        )
}
