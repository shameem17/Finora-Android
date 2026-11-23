package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.viewmodel.SavingsEntryFormViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsFormViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsLogViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsViewModelProtocol
import java.util.UUID

@Composable
fun SavingsView(
    model: SavingsViewModelProtocol,
    onShowDetail: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<SavingsFormViewModel?>(null) }
    var entryForm by remember { mutableStateOf<SavingsEntryFormViewModel?>(null) }
    var log by remember { mutableStateOf<SavingsLogViewModel?>(null) }

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AddActionButton(title = "Add Savings Account", onClick = { form = model.makeFormViewModel() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle("Savings", Modifier.weight(1f))
                IconButton(onClick = { log = model.makeLogViewModel() }, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(
                        imageVector = sfSymbolIcon("clock.arrow.circlepath"),
                        contentDescription = "Savings log",
                        tint = FinoraColors.TextPrimary,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                if (state.items.isEmpty()) {
                    EmptyStateCard(
                        icon = "lock.shield",
                        title = "No savings yet",
                        message = "Create a savings account tagged to your bank or wallet, set a target and watch it grow.",
                    )
                } else {
                    SavingsSummaryCard(state.summary)
                    if (state.summary.breakdown.isNotEmpty()) SavingsBreakdownCard(state.summary.breakdown)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionLabel("Savings accounts")
                        state.items.forEach { item ->
                            SavingsCard(
                                item = item,
                                modifier = Modifier.clickable(role = Role.Button) { onShowDetail(item.id) },
                                onAdd = { entryForm = model.makeEntryFormViewModel(item.id, SavingsEntryKind.Deposit) },
                                onWithdraw = { entryForm = model.makeEntryFormViewModel(item.id, SavingsEntryKind.Withdrawal) },
                            )
                        }
                    }
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) { SavingsFormView(current, onDismiss = { form = null }) }
    }
    entryForm?.let { current ->
        FormSheetHost(onDismissRequest = { entryForm = null }) { SavingsEntryFormView(current, onDismiss = { entryForm = null }) }
    }
    log?.let { current ->
        FormSheetHost(onDismissRequest = { log = null }) { SavingsLogSheet(current, onDismiss = { log = null }) }
    }
}
