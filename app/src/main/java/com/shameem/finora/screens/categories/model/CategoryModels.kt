package com.shameem.finora.screens.categories.model

import com.shameem.finora.core.ui.PickerOption
import java.util.UUID

/** Whether a category groups income or expenses (iOS `CategoryKind`). Persisted by [storageKey]. */
enum class CategoryKind(override val title: String, val storageKey: String) : PickerOption {
    Income("Income", "income"),
    Expense("Expense", "expense");

    /** Lower-case noun used inside sentences ("No income categories"). */
    val singular: String get() = title.lowercase()

    /** SF Symbol names offered in the icon picker for this kind. */
    val icons: List<String>
        get() = when (this) {
            Income -> listOf(
                "banknote", "briefcase.fill", "laptopcomputer", "chart.line.uptrend.xyaxis",
                "gift.fill", "building.columns.fill", "house.fill", "arrow.uturn.backward.circle.fill",
                "percent", "star.fill", "person.2.fill", "storefront.fill",
                "graduationcap.fill", "trophy.fill", "dollarsign.circle.fill", "tray.and.arrow.down.fill",
            )
            Expense -> listOf(
                "fork.knife", "cart.fill", "car.fill", "bus.fill",
                "house.fill", "bolt.fill", "wifi", "iphone",
                "cross.case.fill", "graduationcap.fill", "tshirt.fill", "film.fill",
                "airplane", "gift.fill", "pawprint.fill", "dumbbell.fill",
                "wrench.and.screwdriver.fill", "cup.and.saucer.fill", "gamecontroller.fill", "ellipsis.circle.fill",
            )
        }

    companion object {
        fun fromStorageKey(key: String): CategoryKind? = entries.firstOrNull { it.storageKey == key }
    }
}

/** A user-defined bucket for income or expense entries (iOS `TransactionCategory`). */
data class TransactionCategory(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val kind: CategoryKind,
    val icon: String,
    /** Hex colour, e.g. `#28A745`. */
    val tint: String,
    /** Only expense categories carry a budget; use [normalized] when the source can't be trusted. */
    val monthlyBudget: Double? = null,
) {
    /** Drops the budget of an income category (iOS does this in its initialiser). */
    fun normalized(): TransactionCategory =
        if (kind == CategoryKind.Income && monthlyBudget != null) copy(monthlyBudget = null) else this

    companion object {
        /** Seeded once on first launch (see `DefaultDataSeeder`). */
        val defaults: List<TransactionCategory> = listOf(
            TransactionCategory(name = "Salary", kind = CategoryKind.Income, icon = "briefcase.fill", tint = "#28A745"),
            TransactionCategory(name = "Freelance", kind = CategoryKind.Income, icon = "laptopcomputer", tint = "#17A2B8"),
            TransactionCategory(name = "Business", kind = CategoryKind.Income, icon = "storefront.fill", tint = "#006BDF"),
            TransactionCategory(name = "Gift", kind = CategoryKind.Income, icon = "gift.fill", tint = "#A259FF"),
            TransactionCategory(name = "Investment", kind = CategoryKind.Income, icon = "chart.line.uptrend.xyaxis", tint = "#FFB400"),
            TransactionCategory(name = "Food & Dining", kind = CategoryKind.Expense, icon = "fork.knife", tint = "#FF6B6B", monthlyBudget = 8000.0),
            TransactionCategory(name = "Groceries", kind = CategoryKind.Expense, icon = "cart.fill", tint = "#28A745", monthlyBudget = 6000.0),
            TransactionCategory(name = "Transport", kind = CategoryKind.Expense, icon = "bus.fill", tint = "#006BDF", monthlyBudget = 3000.0),
            TransactionCategory(name = "Bills & Utilities", kind = CategoryKind.Expense, icon = "bolt.fill", tint = "#FFB400", monthlyBudget = 5000.0),
            TransactionCategory(name = "Shopping", kind = CategoryKind.Expense, icon = "tshirt.fill", tint = "#A259FF", monthlyBudget = 4000.0),
            TransactionCategory(name = "Health", kind = CategoryKind.Expense, icon = "cross.case.fill", tint = "#DC3545"),
            TransactionCategory(name = "Entertainment", kind = CategoryKind.Expense, icon = "film.fill", tint = "#00BFA6", monthlyBudget = 2000.0),
        )
    }
}
