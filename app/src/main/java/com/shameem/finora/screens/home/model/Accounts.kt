package com.shameem.finora.screens.home.model

import com.shameem.finora.screens.home.view.AccountType
import java.util.UUID

/**
 * A money account (bank, wallet, card, cash...).
 *
 * [balance] is always calculated: [initialBalance] plus all recorded [netMovement] (transfers, loans,
 * repayments...). Only repositories write [netMovement].
 */
class Account(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val number: String,
    val type: AccountType,
    /** Optional bundled asset name (kept for built-in providers). */
    val logo: String? = null,
    /** User-picked logo, stored as a small PNG. */
    val logoData: ByteArray? = null,
    /** Hex colour, e.g. `#007366`. */
    val tint: String,
    /** What the account held when it was added. Editable. */
    val initialBalance: Double,
    netMovement: Double = 0.0,
) {
    /** Net effect of everything recorded against this account since it was added. */
    var netMovement: Double = netMovement
        private set

    val balance: Double get() = initialBalance + this.netMovement

    /** Copy with the given fields replaced; movement is carried over. */
    fun copy(
        name: String = this.name,
        number: String = this.number,
        type: AccountType = this.type,
        logo: String? = this.logo,
        logoData: ByteArray? = this.logoData,
        tint: String = this.tint,
        initialBalance: Double = this.initialBalance,
    ): Account = Account(id, name, number, type, logo, logoData, tint, initialBalance, this.netMovement)

    /** Returns a copy that records [delta] (negative to debit). */
    fun recordingMovement(delta: Double): Account =
        Account(id, name, number, type, logo, logoData, tint, initialBalance, netMovement + delta)

    /** Copy of `this` keeping [other]'s recorded movement, so editing details never loses history. */
    fun keepingMovement(of: Account): Account =
        Account(id, name, number, type, logo, logoData, tint, initialBalance, of.netMovement)

    override fun equals(other: Any?): Boolean =
        other is Account && id == other.id && name == other.name && number == other.number &&
            type == other.type && logo == other.logo && tint == other.tint &&
            initialBalance == other.initialBalance && netMovement == other.netMovement &&
            (logoData contentEquals other.logoData)

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (logo?.hashCode() ?: 0)
        result = 31 * result + tint.hashCode()
        result = 31 * result + initialBalance.hashCode()
        result = 31 * result + netMovement.hashCode()
        result = 31 * result + (logoData?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String = "Account(id=$id, name=$name, type=$type, balance=$balance)"
}
