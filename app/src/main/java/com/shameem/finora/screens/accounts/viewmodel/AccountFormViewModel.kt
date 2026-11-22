package com.shameem.finora.screens.accounts.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.shameem.finora.core.designsystem.Palette
import com.shameem.finora.core.ui.LogoEditing
import com.shameem.finora.core.util.DecimalInput
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountType
import com.shameem.finora.screens.transaction.AccountRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

/** Editable state of the add / edit account form (iOS `AccountFormViewModel`). */
@Stable
class AccountFormViewModel(
    val mode: Mode,
    private val repository: AccountRepository,
    private val scope: CoroutineScope,
) : LogoEditing {

    sealed interface Mode {
        data object Create : Mode
        data class Edit(val account: Account) : Mode
    }

    val id: UUID = UUID.randomUUID()

    var name: String by mutableStateOf("")
    var number: String by mutableStateOf("")
    var type: AccountType by mutableStateOf(AccountType.Bank)
    var tintHex: String by mutableStateOf(Palette.secondaryHex)
    var initialBalanceText: String by mutableStateOf("")
    override var logoData: ByteArray? by mutableStateOf(null)
    override var isProcessingLogo: Boolean by mutableStateOf(false)

    private var logoAsset: String? by mutableStateOf(null)
    private val previewId = UUID.randomUUID()

    init {
        if (mode is Mode.Edit) {
            val account = mode.account
            name = account.name
            number = account.number
            type = account.type
            tintHex = account.tint
            logoData = account.logoData
            logoAsset = account.logo
            initialBalanceText = DecimalInput.text(account.initialBalance)
        }
    }

    val isEditing: Boolean get() = mode is Mode.Edit
    val title: String get() = if (isEditing) "Edit Account" else "New Account"

    private val trimmedName: String get() = name.trim()
    val initialBalance: Double get() = DecimalInput.value(initialBalanceText)

    /** Activity recorded against the account when the form was opened (0 for a new account). */
    private val movement: Double get() = (mode as? Mode.Edit)?.account?.netMovement ?: 0.0

    val currentBalance: Double get() = initialBalance + movement
    val hasLogo: Boolean get() = logoData != null || logoAsset != null
    val canSubmit: Boolean get() = trimmedName.isNotEmpty() && !isProcessingLogo

    /** Live preview shown above the form. */
    val draft: Account
        get() = Account(
            id = (mode as? Mode.Edit)?.account?.id ?: previewId,
            name = trimmedName.ifEmpty { "Account name" },
            number = number,
            type = type,
            logo = logoAsset,
            logoData = logoData,
            tint = tintHex,
            initialBalance = initialBalance,
            netMovement = movement,
        )

    fun removeLogo() {
        logoData = null
        logoAsset = null
    }

    /** Saves the account. Returns true if the form was valid (so the sheet may close). */
    fun submit(): Boolean {
        if (!canSubmit) return false
        val trimmedNumber = number.trim()
        when (val current = mode) {
            Mode.Create -> {
                val account = Account(
                    name = trimmedName,
                    number = trimmedNumber,
                    type = type,
                    logo = null,
                    logoData = logoData,
                    tint = tintHex,
                    initialBalance = initialBalance,
                )
                scope.launch { repository.add(account) }
            }
            is Mode.Edit -> {
                val account = current.account.copy(
                    name = trimmedName,
                    number = trimmedNumber,
                    type = type,
                    tint = tintHex,
                    logo = logoAsset,
                    logoData = logoData,
                    initialBalance = initialBalance,
                )
                scope.launch { repository.update(account) }
            }
        }
        return true
    }
}
