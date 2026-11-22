package com.shameem.finora.screens.categories.coordinator

import androidx.compose.runtime.Composable
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.coordinator.rememberViewModel
import com.shameem.finora.screens.categories.view.CategoriesView
import com.shameem.finora.screens.categories.viewmodel.CategoriesViewModel

class CategoriesCoordinator(private val dependencies: AppDependencies) : Coordinator {

    @Composable
    override fun Start() {
        val viewModel = rememberViewModel { CategoriesViewModel(dependencies.categoryRepository) }
        CategoriesView(model = viewModel)
    }
}
