package com.shameem.finora.services.persistence

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.model.PersonCategory
import com.shameem.finora.screens.loan.model.Repayment
import com.shameem.finora.screens.transaction.AccountSummary
import java.time.Instant
import java.util.UUID

@Entity(tableName = "people")
class PersonEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryKey: String,
    val phone: String?,
    val address: String?,
) {
    fun toDomain() = Person(
        id = UUID.fromString(id),
        name = name,
        category = PersonCategory.fromStorageKey(categoryKey) ?: PersonCategory.Other,
        phone = phone,
        address = address,
    )

    companion object {
        fun from(person: Person) = PersonEntity(
            id = person.id.toString(),
            name = person.name,
            categoryKey = person.category.storageKey,
            phone = person.phone,
            address = person.address,
        )
    }
}

@Entity(tableName = "loans")
class LoanEntity(
    @PrimaryKey val id: String,
    val kindKey: String,
    val personId: String,
    val accountJson: String,
    val principal: Double,
    val interest: Double,
    val date: Long,
    val dueDate: Long?,
    val installmentCount: Int,
    val note: String?,
    val createdAt: Long,
) {
    fun toDomain(repayments: List<Repayment>) = Loan(
        id = UUID.fromString(id),
        kind = LoanKind.fromStorageKey(kindKey) ?: LoanKind.Lent,
        personId = UUID.fromString(personId),
        account = AccountSnapshot.fromJson(accountJson).summary,
        principal = principal,
        interest = interest,
        date = Instant.ofEpochMilli(date),
        dueDate = dueDate?.let(Instant::ofEpochMilli),
        installmentCount = installmentCount,
        note = note,
        repayments = repayments,
    )

    companion object {
        fun from(loan: Loan, createdAt: Instant = Instant.now()) = LoanEntity(
            id = loan.id.toString(),
            kindKey = loan.kind.storageKey,
            personId = loan.personId.toString(),
            accountJson = AccountSnapshot(loan.account).toJson(),
            principal = loan.principal,
            interest = loan.interest,
            date = loan.date.toEpochMilli(),
            dueDate = loan.dueDate?.toEpochMilli(),
            installmentCount = loan.installmentCount,
            note = loan.note,
            createdAt = createdAt.toEpochMilli(),
        )
    }
}

/** Repayments belong to their loan: deleting the loan cascades to them. */
@Entity(
    tableName = "repayments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("loanId")],
)
class RepaymentEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val amount: Double,
    val date: Long,
    val accountJson: String,
    val note: String?,
    val position: Int,
) {
    fun toDomain() = Repayment(
        id = UUID.fromString(id),
        amount = amount,
        date = Instant.ofEpochMilli(date),
        account = AccountSnapshot.fromJson(accountJson).summary,
        note = note,
    )

    companion object {
        fun from(repayment: Repayment, loanId: UUID, position: Int) = RepaymentEntity(
            id = repayment.id.toString(),
            loanId = loanId.toString(),
            amount = repayment.amount,
            date = repayment.date.toEpochMilli(),
            accountJson = AccountSnapshot(repayment.account).toJson(),
            note = repayment.note,
            position = position,
        )
    }
}

@Dao
interface PersonDao {
    @Query("SELECT * FROM people")
    suspend fun all(): List<PersonEntity>

    @Query("SELECT * FROM people WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): PersonEntity?

    @Insert
    suspend fun insert(entity: PersonEntity)

    @Query("UPDATE people SET name = :name, categoryKey = :categoryKey, phone = :phone, address = :address WHERE id = :id")
    suspend fun updateDetails(id: String, name: String, categoryKey: String, phone: String?, address: String?)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY createdAt")
    suspend fun all(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun byId(id: String): LoanEntity?

    @Query("SELECT * FROM repayments ORDER BY position")
    suspend fun allRepayments(): List<RepaymentEntity>

    @Query("SELECT * FROM repayments WHERE loanId = :loanId ORDER BY position")
    suspend fun repaymentsOf(loanId: String): List<RepaymentEntity>

    @Insert
    suspend fun insert(entity: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRepayments(repayments: List<RepaymentEntity>)

    @Query("DELETE FROM repayments WHERE loanId = :loanId")
    suspend fun deleteRepaymentsOf(loanId: String)

    @Query(
        "UPDATE loans SET personId = :personId, interest = :interest, date = :date, dueDate = :dueDate, " +
            "installmentCount = :installmentCount, note = :note WHERE id = :id",
    )
    suspend fun updateDetails(
        id: String,
        personId: String,
        interest: Double,
        date: Long,
        dueDate: Long?,
        installmentCount: Int,
        note: String?,
    )

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun delete(id: String)
}
