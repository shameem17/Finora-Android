package com.shameem.finora.screens.categories.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.designsystem.Palette
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory
import com.shameem.finora.screens.categories.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.UUID

/** Editable state of the add / edit category form (iOS `CategoryFormViewModel`). */
@Stable
class CategoryFormViewModel(
    val mode: Mode,
    /** Categories of the same kind already stored; used to reject duplicate names without a round trip. */
    private val existing: List<TransactionCategory>,
    private val repository: CategoryRepository,
    private val scope: CoroutineScope,
) {
    sealed interface Mode {
        data class Create(val kind: CategoryKind) : Mode
        data class Edit(val category: TransactionCategory) : Mode
    }

    val kind: CategoryKind = when (mode) {
        is Mode.Create -> mode.kind
        is Mode.Edit -> mode.category.kind
    }

    var name: String by mutableStateOf("")
    var icon: String by mutableStateOf(kind.icons.first())
    var tintHex: String by mutableStateOf(Palette.secondaryHex)
    var budgetText: String by mutableStateOf("")
    var error: String? by mutableStateOf(null)
        private set

    init {
        if (mode is Mode.Edit) {
            val category = mode.category
            name = category.name
            icon = category.icon
            tintHex = category.tint
            budgetText = category.monthlyBudget?.let(DecimalInput::text) ?: ""
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = (if (isEditing) "Edit " else "New ") + kind.title + " Category"
    val supportsBudget: Boolean get() = kind == CategoryKind.Expense

    private val trimmedName: String get() = name.trim()
    private val editingId: UUID? get() = (mode as? Mode.Edit)?.category?.id
    private val budget: Double? get() = DecimalInput.value(budgetText).takeIf { it > 0 }

    val canSubmit: Boolean get() = trimmedName.isNotEmpty()

    /** Live preview row shown above the form. */
    val draft: TransactionCategory
        get() = TransactionCategory(
            id = editingId ?: PREVIEW_ID,
            name = trimmedName.ifEmpty { "Category name" },
            kind = kind,
            icon = icon,
            tint = tintHex,
            monthlyBudget = if (supportsBudget) budget else null,
        )

    /** Saves the category. Returns true if valid (so the sheet may close); otherwise sets [error]. */
    fun submit(): Boolean {
        if (!canSubmit) return false

        val key = comparable(trimmedName)
        val duplicate = existing.any { it.id != editingId && comparable(it.name) == key }
        if (duplicate) {
            error = "You already have a ${kind.singular} category named “$trimmedName”."
            return false
        }
        error = null

        val category = TransactionCategory(
            id = editingId ?: UUID.randomUUID(),
            name = trimmedName,
            kind = kind,
            icon = icon,
            tint = tintHex,
            monthlyBudget = if (supportsBudget) budget else null,
        )
        scope.launch { if (isEditing) repository.update(category) else repository.add(category) }
        return true
    }

    fun delete() {
        val id = editingId ?: return
        scope.launch { repository.delete(id) }
    }

    fun clearError() {
        error = null
    }

    /** Case- and diacritic-insensitive form of a name, for duplicate detection. */
    private fun comparable(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()

    private companion object {
        val PREVIEW_ID: UUID = UUID.randomUUID()
    }
}
