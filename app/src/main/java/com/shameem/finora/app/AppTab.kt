package com.shameem.finora.app

import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.screens.accounts.coordinator.AccountsCoordinator
import com.shameem.finora.screens.categories.coordinator.CategoriesCoordinator
import com.shameem.finora.screens.placeholder.PlaceholderCoordinator
import com.shameem.finora.screens.transaction.TransactionCoordinator

/** The app's sections, in tab order (iOS `AppTab`). Each owns a coordinator and its own back stack. */
enum class AppTab(
    val title: String,
    /** SF Symbol name of the unselected icon; the selected icon is the same name + ".fill". */
    val symbol: String,
) {
    Home("Home", "house"),
    Income("Income", "arrow.down.left.circle"),
    Expense("Expense", "arrow.up.right.circle"),
    Transaction("Transactions", "list.bullet.rectangle"),
    Loan("Loans", "banknote"),
    Savings("Savings", "lock.shield"),
    Accounts("Accounts", "creditcard"),
    Categories("Categories", "square.grid.2x2"),
    Settings("Settings", "gearshape");

    val selectedSymbol: String get() = "$symbol.fill"

    fun coordinator(dependencies: AppDependencies): Coordinator = when (this) {
        Transaction -> TransactionCoordinator(dependencies)
        Accounts -> AccountsCoordinator(dependencies)
        Categories -> CategoriesCoordinator(dependencies)
        // Not ported yet: replaced one by one as each feature lands.
        else -> PlaceholderCoordinator(title = title, symbol = symbol)
    }

    companion object {
        /** Tabs that sit directly on the bar; the rest live under "More" (iOS shows this for >5 tabs). */
        val primary: List<AppTab> = entries.take(4)
        val more: List<AppTab> = entries.drop(4)
    }
}
