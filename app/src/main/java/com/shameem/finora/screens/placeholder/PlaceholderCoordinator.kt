package com.shameem.finora.screens.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shameem.finora.coordinator.Coordinator
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.EmptyStateCard

/** Temporary stand-in for a tab whose feature has not been ported yet. */
class PlaceholderCoordinator(private val title: String, private val symbol: String) : Coordinator {

    @Composable
    override fun Start() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FinoraColors.ScreenBackground)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            EmptyStateCard(icon = symbol, title = title, message = "This section is being ported.")
        }
    }
}
