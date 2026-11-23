package com.shameem.finora.screens.loan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.screens.loan.model.Installment
import com.shameem.finora.screens.loan.model.LoanItem
import com.shameem.finora.screens.loan.model.Repayment
import com.shameem.finora.screens.loan.service.LoanDependencies
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.time.Clock
import java.util.UUID

data class LoanDetailUiState(
    val item: LoanItem? = null,
    val installments: List<Installment> = emptyList(),
    val repayments: List<Repayment> = emptyList(),
    /** False until the first load finishes, so a missing loan isn't mistaken for "not loaded yet". */
    val isLoaded: Boolean = false,
)

interface LoanDetailViewModelProtocol {
    val state: StateFlow<LoanDetailUiState>

    fun load()
    suspend fun makeRepaymentFormViewModel(): RepaymentFormViewModel?
    suspend fun makeEditFormViewModel(): LoanFormViewModel?
    fun deleteRepayment(id: UUID)
    fun deleteLoan()
}

class LoanDetailViewModel(
    private val loanId: UUID,
    private val dependencies: LoanDependencies,
    /** Called after the loan is deleted so the coordinator can leave the detail screen. */
    private val onClose: () -> Unit,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel(), LoanDetailViewModelProtocol {

    private val _state = MutableStateFlow(LoanDetailUiState())
    override val state: StateFlow<LoanDetailUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            merge(dependencies.loans.changes, dependencies.people.changes).collect { load() }
        }
        load()
    }

    override fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val loan = dependencies.loans.loan(loanId)
            _state.value = if (loan == null) {
                LoanDetailUiState(isLoaded = true)
            } else {
                LoanDetailUiState(
                    item = LoanItem.of(loan, dependencies.people.person(loan.personId), clock),
                    installments = loan.installments(clock.zone),
                    repayments = loan.repayments.sortedByDescending { it.date },
                    isLoaded = true,
                )
            }
        }
    }

    override suspend fun makeRepaymentFormViewModel(): RepaymentFormViewModel? {
        val item = _state.value.item?.takeUnless { it.loan.isSettled } ?: return null
        return RepaymentFormViewModel(
            item = item,
            accounts = dependencies.accounts.fetchAccounts(),
            service = dependencies.service,
            scope = viewModelScope,
            zone = clock.zone,
        )
    }

    override suspend fun makeEditFormViewModel(): LoanFormViewModel? {
        val item = _state.value.item ?: return null
        return LoanFormViewModel(
            mode = LoanFormViewModel.Mode.Edit(item.loan),
            accounts = emptyList(),
            people = dependencies.people.fetchPeople(),
            dependencies = dependencies,
            scope = viewModelScope,
            zone = clock.zone,
        )
    }

    override fun deleteRepayment(id: UUID) {
        viewModelScope.launch { dependencies.service.deleteRepayment(id, loanId) }
    }

    override fun deleteLoan() {
        viewModelScope.launch {
            dependencies.service.delete(loanId)
            onClose()
        }
    }
}
