package com.shameem.finora.screens.savings.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.designsystem.Palette
import com.shameem.finora.core.ui.LogoEditing
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.service.SavingsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime
import java.util.UUID

/** Editable state of the create / edit savings form (iOS `SavingsFormViewModel`). */
@Stable
class SavingsFormViewModel(
    val mode: Mode,
    val accounts: List<Account>,
    private val service: SavingsService,
    private val scope: CoroutineScope,
) : LogoEditing {

    sealed interface Mode {
        data object Create : Mode
        data class Edit(val savings: SavingsAccount) : Mode
    }

    var accountId: UUID? by mutableStateOf(null)
        private set
    var name: String by mutableStateOf("")
        private set
    var number: String by mutableStateOf("")
    var tintHex: String by mutableStateOf(Palette.secondaryHex)
    var startDate: Instant by mutableStateOf(Instant.now())
    var targetText: String by mutableStateOf("")
    var hasTargetDate: Boolean by mutableStateOf(false)
    var targetDate: Instant by mutableStateOf(ZonedDateTime.now().plusYears(1).toInstant())
    var openingText: String by mutableStateOf("")
    var note: String by mutableStateOf("")
    override var logoData: ByteArray? by mutableStateOf(null)
    override var isProcessingLogo: Boolean by mutableStateOf(false)

    /** Once the user types a name themselves, picking another account no longer overwrites it. */
    private var nameEdited = false
    private val previewId = UUID.randomUUID()

    init {
        when (mode) {
            Mode.Create -> {
                accountId = accounts.firstOrNull()?.id
                accounts.firstOrNull()?.let { tintHex = it.tint }
                suggestName()
            }
            is Mode.Edit -> {
                val savings = mode.savings
                name = savings.name
                nameEdited = true
                accountId = savings.accountId
                number = savings.number
                tintHex = savings.tint
                logoData = savings.logoData
                startDate = savings.startDate
                targetText = savings.target?.let(DecimalInput::text) ?: ""
                savings.targetDate?.let {
                    hasTargetDate = true
                    targetDate = it
                }
                note = savings.note
            }
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = if (isEditing) "Edit Savings" else "New Savings"
    val saveTitle: String get() = if (isEditing) "Save Changes" else "Create Savings Account"
    val account: Account? get() = accounts.firstOrNull { it.id == accountId }

    val target: Double? get() = DecimalInput.value(targetText).takeIf { it > 0 }
    val openingBalance: Double get() = DecimalInput.value(openingText)
    val hasLogo: Boolean get() = logoData != null

    private val finalName: String get() = SavingsAccount.suffixed(name)
    private val editingId: UUID? get() = (mode as? Mode.Edit)?.savings?.id

    val canSubmit: Boolean get() = accountId != null && finalName.isNotEmpty() && !isProcessingLogo

    /** Live preview card shown above the form. */
    val draft: SavingsAccount
        get() = SavingsAccount(
            id = editingId ?: previewId,
            name = finalName.ifEmpty { "Savings" },
            number = number,
            accountId = accountId ?: previewId,
            logoData = logoData,
            tint = tintHex,
            startDate = startDate,
            target = target,
            openingBalance = if (isEditing) 0.0 else openingBalance,
        )

    fun onNameChange(value: String) {
        name = value
        nameEdited = true
    }

    fun selectAccount(id: UUID) {
        if (accountId == id) return
        accountId = id
        suggestName()
    }

    fun removeLogo() {
        logoData = null
    }

    /** Saves the pot. Returns true if valid (so the sheet may close). */
    fun submit(): Boolean {
        val accountId = accountId
        if (!canSubmit || accountId == null) return false
        val trimmedNumber = number.trim()
        val trimmedNote = note.trim()
        val dueDate = if (hasTargetDate) maxOf(targetDate, startDate) else null

        when (val current = mode) {
            Mode.Create -> scope.launch {
                service.create(
                    SavingsAccount(
                        name = finalName,
                        number = trimmedNumber,
                        accountId = accountId,
                        logoData = logoData,
                        tint = tintHex,
                        startDate = startDate,
                        target = target,
                        targetDate = dueDate,
                        note = trimmedNote,
                        openingBalance = openingBalance,
                    ),
                )
            }
            is Mode.Edit -> scope.launch {
                service.update(
                    current.savings.copy(
                        name = finalName,
                        number = trimmedNumber,
                        accountId = accountId,
                        logoData = logoData,
                        tint = tintHex,
                        startDate = startDate,
                        target = target,
                        targetDate = dueDate,
                        note = trimmedNote,
                    ),
                )
            }
        }
        return true
    }

    /** Pre-fills "<Account> Savings" until the user chooses their own name. */
    private fun suggestName() {
        if (nameEdited) return
        val account = account ?: return
        name = SavingsAccount.suffixed(account.name)
        if (!isEditing) tintHex = account.tint
    }
}
