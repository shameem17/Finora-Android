package com.shameem.finora.coordinator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shameem.finora.app.AppDependencies
import com.shameem.finora.app.AppTab
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon

/**
 * Hosts the tab bar. Every tab has its own coordinator (and therefore its own back stack), created once
 * per [MainCoordinator]. With nine tabs, only the first four sit on the bar and the rest are listed under
 * "More", exactly like the iOS `UITabBarController`.
 */
class MainCoordinator(private val dependencies: AppDependencies) : Coordinator {

    private val coordinators: Map<AppTab, Coordinator> =
        AppTab.entries.associateWith { it.coordinator(dependencies) }

    @Composable
    override fun Start() {
        var selectedName by rememberSaveable { mutableStateOf(AppTab.Home.name) }
        // When non-null, a tab from the "More" list is open; null shows the list itself.
        var openedMoreName by rememberSaveable { mutableStateOf<String?>(null) }
        val stateHolder = rememberSaveableStateHolder()

        val selected = AppTab.valueOf(selectedName)
        val openedMore = openedMoreName?.let(AppTab::valueOf)
        val showingMoreSection = selected !in AppTab.primary

        // Back unwinds: opened More tab -> More list -> Home -> leave the app.
        BackHandler(enabled = showingMoreSection || selected != AppTab.Home) {
            when {
                showingMoreSection && openedMore != null -> openedMoreName = null
                else -> {
                    selectedName = AppTab.Home.name
                    openedMoreName = null
                }
            }
        }

        Scaffold(
            containerColor = FinoraColors.ScreenBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                FinoraTabBar(
                    selected = selected,
                    onPrimarySelected = {
                        selectedName = it.name
                        openedMoreName = null
                    },
                    onMoreSelected = {
                        selectedName = AppTab.more.first().name
                        openedMoreName = null
                    },
                )
            },
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
                when {
                    !showingMoreSection -> TabContent(selected, stateHolder)
                    openedMore == null -> MoreList(onOpen = {
                        selectedName = it.name
                        openedMoreName = it.name
                    })
                    else -> Column(Modifier.fillMaxSize()) {
                        MoreBackBar(onBack = { openedMoreName = null })
                        Box(Modifier.weight(1f)) { TabContent(openedMore, stateHolder) }
                    }
                }
            }
        }
    }

    @Composable
    private fun TabContent(tab: AppTab, stateHolder: SaveableStateHolder) {
        // Only the selected tab is composed; its UI state is saved/restored per tab.
        stateHolder.SaveableStateProvider(tab.name) { coordinators.getValue(tab).Start() }
    }
}

/** Opaque teal bar; cream icons at 60%, tangerine when selected (iOS `AppAppearance`). */
@Composable
private fun FinoraTabBar(
    selected: AppTab,
    onPrimarySelected: (AppTab) -> Unit,
    onMoreSelected: () -> Unit,
) {
    val normal = FinoraColors.BrandPrimary.copy(alpha = 0.6f)
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = FinoraColors.BrandAccent,
        selectedTextColor = FinoraColors.BrandAccent,
        unselectedIconColor = normal,
        unselectedTextColor = normal,
        indicatorColor = Color.Transparent,
    )

    Column {
        HorizontalDivider(color = FinoraColors.BrandPrimary.copy(alpha = 0.12f))
        NavigationBar(containerColor = FinoraColors.BrandSecondary, tonalElevation = 0.dp) {
            AppTab.primary.forEach { tab ->
                val isSelected = tab == selected
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onPrimarySelected(tab) },
                    icon = {
                        Icon(
                            imageVector = sfSymbolIcon(if (isSelected) tab.selectedSymbol else tab.symbol),
                            contentDescription = tab.title,
                        )
                    },
                    label = { AppText(tab.title, size = 10, weight = AppFontWeight.Medium, maxLines = 1) },
                    colors = colors,
                )
            }
            val moreSelected = selected !in AppTab.primary
            NavigationBarItem(
                selected = moreSelected,
                onClick = onMoreSelected,
                icon = {
                    Icon(
                        imageVector = sfSymbolIcon("ellipsis"),
                        contentDescription = "More",
                    )
                },
                label = { AppText("More", size = 10, weight = AppFontWeight.Medium, maxLines = 1) },
                colors = colors,
            )
        }
    }
}

/** The "More" tab: the tabs that don't fit on the bar, as a list. */
@Composable
private fun MoreList(onOpen: (AppTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FinoraColors.ScreenBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("More")
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .finoraCard(cornerRadius = 24.dp),
        ) {
            AppTab.more.forEachIndexed { index, tab ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button) { onOpen(tab) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = sfSymbolIcon(tab.symbol),
                        contentDescription = null,
                        tint = FinoraColors.TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                    AppText(
                        text = tab.title,
                        size = 17,
                        weight = AppFontWeight.SemiBold,
                        color = FinoraColors.TextPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = sfSymbolIcon("chevron.right"),
                        contentDescription = null,
                        tint = FinoraColors.TextTertiary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (index != AppTab.more.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
            }
        }
    }
}

@Composable
private fun MoreBackBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FinoraColors.ScreenBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .clickable(role = Role.Button, onClick = onBack)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = FinoraColors.TextPrimary,
        )
        AppText("More", size = 17, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
    }
}
