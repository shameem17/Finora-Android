package com.shameem.finora.screens.accounts.view

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.accounts.viewmodel.AccountFormViewModel
import com.shameem.finora.screens.accounts.viewmodel.AccountsViewModelProtocol
import com.shameem.finora.screens.home.model.Account
import com.shameem.finora.screens.transaction.AccountBadge
import java.util.UUID

@Composable
fun AccountsView(
    model: AccountsViewModelProtocol,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<AccountFormViewModel?>(null) }
    var frontIdText by rememberSaveable { mutableStateOf<String?>(null) }
    val frontId: UUID? = frontIdText?.let(UUID::fromString)

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AddActionButton(title = "Add New Account", onClick = { form = model.makeFormViewModel(null) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
        ) {
            ScreenTitle("Accounts")
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TotalCard(totalBalance = state.totalBalance, count = state.accounts.size)
                if (state.accounts.isEmpty()) {
                    EmptyStateCard(
                        icon = "creditcard",
                        title = "No accounts yet",
                        message = "Add your bank, mobile wallet, card or cash to start tracking balances.",
                    )
                } else {
                    AccountCardStack(
                        accounts = state.accounts,
                        frontId = frontId,
                        onFrontChange = { frontIdText = it?.toString() },
                        onEdit = { form = model.makeFormViewModel(it) },
                    )
                    AccountList(
                        accounts = state.accounts,
                        frontId = frontId,
                        onSelect = { frontIdText = it.toString() },
                    )
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) {
            AccountFormView(form = current, onDismiss = { form = null })
        }
    }
}

@Composable
private fun TotalCard(totalBalance: Double, count: Int) {
    Column(
        modifier = Modifier.sectionCard(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SectionLabel("Total balance")
        ShrinkToFitText(
            text = CurrencyFormatter.taka(totalBalance, showFraction = true),
            style = appTextStyle(size = 32, weight = AppFontWeight.ExtraBold),
            color = FinoraColors.TextPrimary,
            minScale = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(
            text = if (count == 1) "1 account" else "$count accounts",
            size = 13,
            weight = AppFontWeight.Medium,
            color = FinoraColors.TextSecondary,
        )
    }
}

@Composable
private fun AccountList(
    accounts: List<Account>,
    frontId: UUID?,
    onSelect: (UUID) -> Unit,
) {
    Column(Modifier.sectionCard(padding = 8.dp)) {
        accounts.forEachIndexed { index, account ->
            AccountRow(account = account, isFront = frontId == account.id, onClick = { onSelect(account.id) })
            if (index != accounts.lastIndex) HorizontalDivider(color = FinoraColors.Hairline)
        }
    }
}

@Composable
private fun AccountRow(account: Account, isFront: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccountBadge(type = account.type, tint = account.tint, size = 36.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ShrinkToFitText(
                text = account.name,
                style = appTextStyle(size = 15, weight = AppFontWeight.Bold),
                color = FinoraColors.TextPrimary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
            AppText(
                text = account.type.title,
                size = 12,
                weight = AppFontWeight.Medium,
                color = FinoraColors.TextSecondary,
            )
        }
        AppText(
            text = CurrencyFormatter.taka(account.balance, showFraction = true),
            size = 15,
            weight = AppFontWeight.Bold,
            color = FinoraColors.TextPrimary,
            maxLines = 1,
        )
        Icon(
            imageVector = sfSymbolIcon(if (isFront) "checkmark.circle.fill" else "circle"),
            contentDescription = null,
            tint = if (isFront) FinoraColors.Accent else FinoraColors.TextTertiary,
        )
    }
}
