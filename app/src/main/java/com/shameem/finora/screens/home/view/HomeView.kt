package com.shameem.finora.screens.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.util.TakaFormatter
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.home.model.SummaryType
import com.shameem.finora.screens.home.viewmodel.HomeViewModelProtocol

@Composable
fun HomeView(model: HomeViewModelProtocol, modifier: Modifier = Modifier) {
    val data by model.homeViewData.collectAsStateWithLifecycle()

    LaunchedEffect(model) { model.prepareData() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FinoraColors.ScreenBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitle("Home")
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                AppText(
                    data?.headerTitle ?: " ",
                    size = 20, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SummaryType.entries.forEach { type ->
                        SummaryCard(
                            type = type,
                            amount = data?.insights?.let { TakaFormatter.string(it.amount(type)) } ?: "—",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            data?.let { loaded ->
                Section("Insights", trailing = {
                    AppText(
                        "This month",
                        size = 13, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextSecondary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FinoraColors.FillSubtle)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }) { InsightsCard(loaded.insights) }
                Section("Summary") { FinanceOverviewCard(loaded.overview) }
            }
            Section("Accounts") { HomeAccountGrid(data?.accounts.orEmpty()) }
        }
    }
}

@Composable
private fun Section(
    title: String,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppText(title, size = 20, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, modifier = Modifier.weight(1f))
            trailing()
        }
        content()
    }
}

/** Two cards per row (iOS adaptive grid with a 160pt minimum). */
@Composable
private fun HomeAccountGrid(accounts: List<Account>) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        accounts.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                pair.forEach { account ->
                    val glow = colorFromHex(account.tint).copy(alpha = 0.35f)
                    AccountCard(
                        account = account,
                        modifier = Modifier
                            .weight(1f)
                            .shadow(10.dp, ambientColor = glow, spotColor = glow, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)),
                    )
                }
                if (pair.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}
