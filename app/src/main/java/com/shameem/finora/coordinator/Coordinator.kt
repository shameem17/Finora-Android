package com.shameem.finora.coordinator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController

/**
 * A coordinator owns one navigation flow: it creates view models, wires dependencies and decides
 * which screen is shown. Screens never know about each other. This is the Compose counterpart of
 * the iOS `Coordinator` protocol (`start()` there, [Start] here because it emits UI).
 */
interface Coordinator {
    @Composable
    fun Start()
}

/**
 * Hosts a tab's own navigation back stack. The [NavHostController] state is saveable, so a tab
 * keeps its stack while the user visits other tabs.
 */
@Composable
internal fun TabNavHost(
    startDestination: String,
    builder: NavGraphBuilder.(NavHostController) -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        builder(navController)
    }
}

/**
 * Creates (or returns the retained) [ViewModel] using [create] for first-time construction. Pass a [key] when
 * two screens use the same ViewModel class (e.g. the Income and Expense tabs), or they would share one instance.
 */
@Composable
internal inline fun <reified VM : ViewModel> rememberViewModel(
    key: String? = null,
    noinline create: () -> VM,
): VM = viewModel(key = key, factory = remember { viewModelFactory { initializer { create() } } })
