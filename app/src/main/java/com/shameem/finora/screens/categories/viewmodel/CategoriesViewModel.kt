package com.shameem.finora.screens.categories.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.categories.repository.CategoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator

data class CategoriesUiState(
    val kind: CategoryKind = CategoryKind.Expense,
    val categories: List<TransactionCategory> = emptyList(),
) {
    val totalBudget: Double get() = categories.sumOf { it.monthlyBudget ?: 0.0 }
    val budgetedCount: Int get() = categories.count { it.monthlyBudget != null }
}

interface CategoriesViewModelProtocol {
    val state: StateFlow<CategoriesUiState>

    fun selectKind(kind: CategoryKind)
    fun load()
    fun makeFormViewModel(editing: TransactionCategory?): CategoryFormViewModel
}

class CategoriesViewModel(
    private val repository: CategoryRepository,
) : ViewModel(), CategoriesViewModelProtocol {

    private val _state = MutableStateFlow(CategoriesUiState())
    override val state: StateFlow<CategoriesUiState> = _state.asStateFlow()

    private val collator = Collator.getInstance().apply { strength = Collator.SECONDARY }
    private var loadJob: Job? = null

    init {
        viewModelScope.launch { repository.changes.collect { load() } }
        load()
    }

    override fun selectKind(kind: CategoryKind) {
        if (_state.value.kind == kind) return
        _state.update { it.copy(kind = kind) }
        load()
    }

    override fun load() {
        loadJob?.cancel()
        val kind = _state.value.kind
        loadJob = viewModelScope.launch {
            val sorted = repository.fetchCategories(kind).sortedWith { a, b -> collator.compare(a.name, b.name) }
            // Ignore a result for a kind the user has already switched away from.
            _state.update { if (it.kind == kind) it.copy(categories = sorted) else it }
        }
    }

    override fun makeFormViewModel(editing: TransactionCategory?): CategoryFormViewModel =
        CategoryFormViewModel(
            mode = editing?.let { CategoryFormViewModel.Mode.Edit(it) }
                ?: CategoryFormViewModel.Mode.Create(_state.value.kind),
            existing = _state.value.categories,
            repository = repository,
            scope = viewModelScope,
        )
}
