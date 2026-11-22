package com.shameem.finora.coordinator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.app.AppTab
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText

/**
 * Hosts the tab bar. Every tab has its own coordinator (and therefore its own back stack),
 * created once per [MainCoordinator]. (iOS `MainCoordinator` + `UITabBarController`.)
 */
class MainCoordinator(dependencies: AppDependencies) : Coordinator {

    private val coordinators: Map<AppTab, Coordinator> =
        AppTab.entries.associateWith { tab -> tab.coordinator(dependencies) }

    @Composable
    override fun Start() {
        var selectedTab by rememberSaveable { mutableStateOf(AppTab.Home) }
        val stateHolder = rememberSaveableStateHolder()

        // Back from a secondary tab returns to Home before leaving the app.
        BackHandler(enabled = selectedTab != AppTab.Home) { selectedTab = AppTab.Home }

        Scaffold(
            containerColor = FinoraColors.Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                FinoraNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            },
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                // Only the selected tab is composed; its UI state is saved/restored per tab.
                stateHolder.SaveableStateProvider(key = selectedTab.name) {
                    coordinators.getValue(selectedTab).Start()
                }
            }
        }
    }
}

@Composable
private fun FinoraNavigationBar(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
) {
    val darkGray = CustomColor.DarkGray.color

    NavigationBar(
        containerColor = CustomColor.CustomWhite.color,
        tonalElevation = 0.dp,
    ) {
        AppTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                    )
                },
                label = {
                    AppText(
                        text = tab.title,
                        size = 10,
                        weight = if (selected) AppFontWeight.Bold else AppFontWeight.Medium,
                        maxLines = 1,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = darkGray,
                    selectedTextColor = darkGray,
                    unselectedIconColor = darkGray.copy(alpha = 0.55f),
                    unselectedTextColor = darkGray.copy(alpha = 0.55f),
                    indicatorColor = darkGray.copy(alpha = 0.10f),
                ),
            )
        }
    }
}
