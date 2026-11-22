package com.shameem.finora.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.finoraCard
import kotlinx.coroutines.launch

/** iOS hard-codes this warm background on the Transactions screen (`Color(0.96, 0.94, 0.90)`). */
private val TransactionBackground = Color(red = 0.96f, green = 0.94f, blue = 0.90f)

@Composable
fun TransactionView(
    model: TransactionViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<TransferFormViewModel?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TransactionBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AddActionButton(
                title = "New Transfer",
                icon = "arrow.left.arrow.right",
                onClick = { scope.launch { form = model.makeFormViewModel() } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(TransactionBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PillPicker(
                options = TransferPeriod.entries,
                selection = state.period,
                onSelectionChange = model::setPeriod,
            )
            TransferSummaryCard(period = state.period, summary = state.summary)
            TransferHistory(sections = state.sections, period = state.period)
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) {
            TransferFormView(form = current, onDismiss = { form = null })
        }
    }
}

@Composable
private fun TransferHistory(sections: List<TransferSection>, period: TransferPeriod) {
    if (sections.isEmpty()) {
        EmptyTransfersView(period = period)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        sections.forEach { section ->
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppText(
                    text = section.title,
                    size = 16,
                    weight = AppFontWeight.Bold,
                    color = FinoraColors.TextSecondary,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Column(
                    Modifier
                        .fillMaxWidth()
                        .finoraCard(cornerRadius = 24.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    section.transfers.forEachIndexed { index, transfer ->
                        TransferRow(transfer = transfer)
                        if (index != section.transfers.lastIndex) {
                            HorizontalDivider(color = FinoraColors.Divider)
                        }
                    }
                }
            }
        }
    }
}
