package com.shameem.finora.services.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.view.AccountType
import com.shameem.finora.screens.transaction.AccountSummary
import com.shameem.finora.screens.transaction.Transfer
import org.json.JSONObject
import java.time.Instant
import java.util.UUID

/**
 * Codable copy of [AccountSummary], stored inline in loans, repayments, transfers... as a JSON string
 * (iOS `AccountSnapshot`).
 */
data class AccountSnapshot(
    val id: UUID,
    val name: String,
    val tint: String,
    val typeKey: String,
) {
    constructor(summary: AccountSummary) : this(summary.id, summary.name, summary.tint, summary.type.storageKey)

    val summary: AccountSummary
        get() = AccountSummary(id, name, tint, AccountType.fromStorageKey(typeKey) ?: AccountType.Unknown)

    fun toJson(): String = JSONObject()
        .put("id", id.toString())
        .put("name", name)
        .put("tint", tint)
        .put("typeKey", typeKey)
        .toString()

    companion object {
        fun fromJson(json: String): AccountSnapshot = JSONObject(json).let {
            AccountSnapshot(
                id = UUID.fromString(it.getString("id")),
                name = it.getString("name"),
                tint = it.getString("tint"),
                typeKey = it.getString("typeKey"),
            )
        }
    }
}

@Entity(tableName = "accounts")
class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val number: String,
    val typeKey: String,
    val logo: String?,
    val logoData: ByteArray?,
    val tint: String,
    val initialBalance: Double,
    val netMovement: Double,
    val createdAt: Long,
) {
    fun toDomain(): Account = Account(
        id = UUID.fromString(id),
        name = name,
        number = number,
        type = AccountType.fromStorageKey(typeKey) ?: AccountType.Unknown,
        logo = logo,
        logoData = logoData,
        tint = tint,
        initialBalance = initialBalance,
        netMovement = netMovement,
    )

    companion object {
        fun from(account: Account, createdAt: Instant = Instant.now()) = AccountEntity(
            id = account.id.toString(),
            name = account.name,
            number = account.number,
            typeKey = account.type.storageKey,
            logo = account.logo,
            logoData = account.logoData,
            tint = account.tint,
            initialBalance = account.initialBalance,
            netMovement = account.netMovement,
            createdAt = createdAt.toEpochMilli(),
        )
    }
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt")
    suspend fun all(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): AccountEntity?

    @Insert
    suspend fun insert(entity: AccountEntity)

    @Query(
        "UPDATE accounts SET name = :name, number = :number, typeKey = :typeKey, logo = :logo, " +
            "logoData = :logoData, tint = :tint, initialBalance = :initialBalance WHERE id = :id",
    )
    suspend fun updateDetails(
        id: String,
        name: String,
        number: String,
        typeKey: String,
        logo: String?,
        logoData: ByteArray?,
        tint: String,
        initialBalance: Double,
    )

    /** Atomic increment, so concurrent writers can never lose an update. */
    @Query("UPDATE accounts SET netMovement = netMovement + :delta WHERE id = :id")
    suspend fun addMovement(id: String, delta: Double)
}

@Entity(tableName = "transfers")
class TransferEntity(
    @PrimaryKey val id: String,
    val sourceJson: String,
    val destinationJson: String,
    val amount: Double,
    val charge: Double,
    val date: Long,
    val reason: String,
) {
    fun toDomain(): Transfer = Transfer(
        id = UUID.fromString(id),
        from = AccountSnapshot.fromJson(sourceJson).summary,
        to = AccountSnapshot.fromJson(destinationJson).summary,
        amount = amount,
        charge = charge,
        date = Instant.ofEpochMilli(date),
        reason = reason,
    )

    companion object {
        fun from(transfer: Transfer) = TransferEntity(
            id = transfer.id.toString(),
            sourceJson = AccountSnapshot(transfer.from).toJson(),
            destinationJson = AccountSnapshot(transfer.to).toJson(),
            amount = transfer.amount,
            charge = transfer.charge,
            date = transfer.date.toEpochMilli(),
            reason = transfer.reason,
        )
    }
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY date DESC")
    suspend fun allNewestFirst(): List<TransferEntity>

    @Insert
    suspend fun insert(entity: TransferEntity)
}
