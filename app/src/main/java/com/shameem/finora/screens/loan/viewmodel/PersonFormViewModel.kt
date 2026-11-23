package com.shameem.finora.screens.loan.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.model.PersonCategory
import com.shameem.finora.screens.loan.repository.PersonRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.UUID

object PhoneInput {
    /** Keeps digits, "+", "-" and spaces, capped at [maxLength]. */
    fun sanitize(raw: String, maxLength: Int = 20): String =
        raw.filter { it.isDigit() || it in "+- " }.take(maxLength)
}

/** Editable state of the add / edit person form (iOS `PersonFormViewModel`). */
@Stable
class PersonFormViewModel(
    val mode: Mode,
    private val repository: PersonRepository,
    private val scope: CoroutineScope,
    /** People already saved, for the duplicate-name check. */
    private val existingPeople: List<Person>,
    val linkedLoanCount: Int = 0,
    private val onSaved: (Person) -> Unit = {},
    private val onDeleted: () -> Unit = {},
) {
    sealed interface Mode {
        data object Create : Mode
        data class Edit(val person: Person) : Mode
    }

    var name: String by mutableStateOf("")
    var category: PersonCategory by mutableStateOf(PersonCategory.Individual)
    var phone: String by mutableStateOf("")
    var address: String by mutableStateOf("")

    init {
        if (mode is Mode.Edit) {
            name = mode.person.name
            category = mode.person.category
            phone = mode.person.phone ?: ""
            address = mode.person.address ?: ""
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = if (isEditing) "Edit Person" else "New Person"

    private val editingId: UUID? get() = (mode as? Mode.Edit)?.person?.id
    private val trimmedName: String get() = name.trim()

    val duplicateError: String?
        get() {
            val key = comparable(trimmedName)
            val clash = existingPeople.any { it.id != editingId && comparable(it.name) == key }
            return if (clash) "Someone with this name is already saved." else null
        }

    val canSubmit: Boolean get() = trimmedName.isNotEmpty() && duplicateError == null
    val canDelete: Boolean get() = isEditing && linkedLoanCount == 0

    val deleteBlockedReason: String?
        get() {
            if (!isEditing || linkedLoanCount == 0) return null
            val noun = if (linkedLoanCount == 1) "entry" else "entries"
            return "Linked to $linkedLoanCount loan $noun. Delete or settle them first to remove this person."
        }

    /** Saves the person. Returns true if valid (so the sheet may close). */
    fun submit(): Boolean {
        if (!canSubmit) return false
        val phone = phone.trim().ifEmpty { null }
        val address = address.trim().ifEmpty { null }
        val person = when (val current = mode) {
            Mode.Create -> Person(name = trimmedName, category = category, phone = phone, address = address)
            is Mode.Edit -> current.person.copy(name = trimmedName, category = category, phone = phone, address = address)
        }
        scope.launch {
            if (isEditing) repository.update(person) else repository.add(person)
            onSaved(person)
        }
        return true
    }

    fun delete(): Boolean {
        val id = editingId
        if (!canDelete || id == null) return false
        scope.launch {
            repository.delete(id)
            onDeleted()
        }
        return true
    }

    private fun comparable(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()
}
