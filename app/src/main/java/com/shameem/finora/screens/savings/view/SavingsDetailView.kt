package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ConfirmDeleteDialog
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.ProgressBar
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.StatColumn
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.savings.model.SavingsAccount
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.model.SavingsItem
import com.shameem.finora.screens.savings.viewmodel.SavingsDetailViewModelProtocol
import com.shameem.finora.screens.savings.viewmodel.SavingsEntryFormViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsFormViewModel
import com.shameem.finora.screens.savings.viewmodel.SavingsLogViewModel
import com.shameem.finora.services.date.shortDisplay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SavingsDetailView(
    model: SavingsDetailViewModelProtocol,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    val item = state.item
    val scope = rememberCoroutineScope()

    var entryForm by remember { mutableStateOf<SavingsEntryFormViewModel?>(null) }
    var log by remember { mutableStateOf<SavingsLogViewModel?>(null) }
    var editForm by remember { mutableStateOf<SavingsFormViewModel?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<SavingsEntry?>(null) }
    var entryDeleteFailed by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(model) { model.load() }
    // The pot was deleted elsewhere (or just now): nothing left to show.
    LaunchedEffect(state.isLoaded, item == null) { if (state.isLoaded && item == null) onBack() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FinoraColors.TextPrimary)
                }
                AppText(
                    item?.savings?.name ?: "Savings",
                    size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary,
                    maxLines = 1, modifier = Modifier.weight(1f),
                )
                if (item != null) {
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(sfSymbolIcon("ellipsis.circle"), contentDescription = "More", tint = FinoraColors.TextPrimary)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { AppText("Edit", size = 15, color = FinoraColors.TextPrimary) },
                                leadingIcon = { Icon(sfSymbolIcon("pencil"), null, tint = FinoraColors.TextPrimary) },
                                onClick = {
                                    menuOpen = false
                                    editForm = model.makeEditFormViewModel()
                                },
                            )
                            DropdownMenuItem(
                                text = { AppText("Delete", size = 15, color = FinoraColors.Negative) },
                                leadingIcon = { Icon(sfSymbolIcon("trash"), null, tint = FinoraColors.Negative) },
                                onClick = {
                                    menuOpen = false
                                    confirmDelete = true
                                },
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomActions(
                canWithdraw = (item?.savings?.currentSavings ?: 0.0) > 0,
                enabled = item != null,
                onWithdraw = { entryForm = model.makeEntryFormViewModel(SavingsEntryKind.Withdrawal) },
                onDeposit = { entryForm = model.makeEntryFormViewModel(SavingsEntryKind.Deposit) },
            )
        },
    ) { padding ->
        if (item != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Header(item.savings)
                Details(item)
                History(
                    entries = state.entries,
                    onFullLog = { log = model.makeLogViewModel() },
                    onEdit = { entryForm = model.makeEditEntryFormViewModel(it) },
                    onDelete = { entryToDelete = it },
                )
            }
        }
    }

    entryForm?.let { current ->
        FormSheetHost(onDismissRequest = { entryForm = null }) { SavingsEntryFormView(current, onDismiss = { entryForm = null }) }
    }
    editForm?.let { current ->
        FormSheetHost(onDismissRequest = { editForm = null }) { SavingsFormView(current, onDismiss = { editForm = null }) }
    }
    log?.let { current ->
        FormSheetHost(onDismissRequest = { log = null }) { SavingsLogSheet(current, onDismiss = { log = null }) }
    }

    if (confirmDelete && item != null) {
        DeleteSavingsDialog(
            item = item,
            onDeleteAndReturn = {
                confirmDelete = false
                model.deleteSavings(returnRemaining = true)
            },
            onDeleteOnly = {
                confirmDelete = false
                model.deleteSavings(returnRemaining = false)
            },
            onDismiss = { confirmDelete = false },
        )
    }

    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = "Delete this entry?",
            message = entry.account?.let {
                "The ${it.name} balance is adjusted back by ${CurrencyFormatter.taka(entry.amount, showFraction = true)}."
            } ?: "No account balance is affected.",
            onConfirm = {
                entryToDelete = null
                scope.launch { if (!model.deleteEntry(entry.id)) entryDeleteFailed = true }
            },
            onDismiss = { entryToDelete = null },
        )
    }

    if (entryDeleteFailed) {
        AlertDialog(
            onDismissRequest = { entryDeleteFailed = false },
            containerColor = FinoraColors.ScreenBackground,
            title = { AppText("Can't delete this entry", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
            text = { AppText("Removing it would leave you with more withdrawn than saved.", size = 14, color = FinoraColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { entryDeleteFailed = false }) {
                    AppText("OK", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                }
            },
        )
    }
}

@Composable
private fun DeleteSavingsDialog(
    item: SavingsItem,
    onDeleteAndReturn: () -> Unit,
    onDeleteOnly: () -> Unit,
    onDismiss: () -> Unit,
) {
    val remaining = item.savings.currentSavings
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FinoraColors.ScreenBackground,
        title = { AppText("Delete this savings account?", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
        text = {
            AppText(
                "Returning sends what's left back to ${item.account?.name ?: "its account"}. " +
                    "“Delete only” leaves all account balances as they are.",
                size = 14, color = FinoraColors.TextSecondary,
            )
        },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (remaining > 0) {
                    TextButton(onClick = onDeleteAndReturn) {
                        AppText(
                            "Delete & return ${CurrencyFormatter.taka(remaining, showFraction = true)}",
                            size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative,
                        )
                    }
                }
                TextButton(onClick = onDeleteOnly) {
                    AppText("Delete only", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                AppText("Cancel", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
            }
        },
    )
}

@Composable
private fun BottomActions(
    canWithdraw: Boolean,
    enabled: Boolean,
    onWithdraw: () -> Unit,
    onDeposit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FinoraColors.ScreenBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val withdrawEnabled = enabled && canWithdraw
        ActionPill(
            title = "Withdraw",
            icon = SavingsEntryKind.Withdrawal.icon,
            foreground = FinoraColors.TextPrimary,
            background = FinoraColors.FillSubtle,
            enabled = withdrawEnabled,
            onClick = onWithdraw,
            modifier = Modifier.weight(1f),
        )
        ActionPill(
            title = "Add Savings",
            icon = SavingsEntryKind.Deposit.icon,
            foreground = FinoraColors.OnAccent,
            background = FinoraColors.Accent,
            enabled = enabled,
            onClick = onDeposit,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionPill(
    title: String,
    icon: String,
    foreground: Color,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(sfSymbolIcon(icon), contentDescription = null, tint = foreground, modifier = Modifier.size(18.dp))
        AppText(title, size = 16, weight = AppFontWeight.Bold, color = foreground)
    }
}

@Composable
private fun Header(savings: SavingsAccount) {
    val tint = remember(savings.tint) { colorFromHex(savings.tint) }
    Column(Modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SavingsLogo(tint = savings.tint, logoData = savings.logoData, size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                AppText(
                    "CURRENT SAVINGS", size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                    letterSpacing = 1.sp,
                )
                ShrinkToFitText(
                    text = CurrencyFormatter.taka(savings.currentSavings, showFraction = true),
                    style = appTextStyle(size = 30, weight = AppFontWeight.ExtraBold),
                    color = FinoraColors.TextPrimary,
                    minScale = 0.5f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        val progress = savings.progress
        val target = savings.target
        if (progress != null && target != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProgressBar(fraction = progress, color = tint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    AppText(
                        "${(progress * 100).roundToInt()}% of ${CurrencyFormatter.taka(target)}",
                        size = 12, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                    )
                    if (savings.isGoalReached) {
                        GoalReachedLabel(size = 12)
                    } else {
                        savings.remainingToTarget?.let {
                            AppText(
                                "${CurrencyFormatter.taka(it, showFraction = true)} to go",
                                size = 12, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        Row(Modifier.fillMaxWidth()) {
            StatColumn("Total saved", CurrencyFormatter.taka(savings.totalDeposited, showFraction = true), Modifier.weight(1f))
            StatColumn("Withdrawn", CurrencyFormatter.taka(savings.totalWithdrawn, showFraction = true), Modifier.weight(1f))
        }
    }
}

@Composable
private fun Details(item: SavingsItem) {
    val savings = item.savings
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 22.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ValueRow("Tagged account", item.account?.name ?: "—")
        if (savings.number.isNotEmpty()) ValueRow("Account no.", savings.number)
        ValueRow("Started", savings.startDate.shortDisplay())
        savings.targetDate?.let { ValueRow("Target date", it.shortDisplay()) }
        savings.target?.let { ValueRow("Target", CurrencyFormatter.taka(it, showFraction = true)) }
        if (savings.openingBalance > 0) {
            ValueRow("Opening balance", CurrencyFormatter.taka(savings.openingBalance, showFraction = true))
        }
        if (savings.note.isNotEmpty()) ValueRow("Note", savings.note)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun History(
    entries: List<SavingsEntry>,
    onFullLog: () -> Unit,
    onEdit: (SavingsEntry) -> Unit,
    onDelete: (SavingsEntry) -> Unit,
) {
    Column(Modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionLabel("History", Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                AppText(
                    "Full log", size = 13, weight = AppFontWeight.Bold, color = FinoraColors.BrandSecondary,
                    modifier = Modifier.clickable(role = Role.Button, onClick = onFullLog),
                )
            }
        }
        if (entries.isEmpty()) {
            AppText("No deposits or withdrawals yet.", size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
        } else {
            entries.forEachIndexed { index, entry ->
                var menu by remember { mutableStateOf(false) }
                Box {
                    SavingsEntryRow(
                        entry = entry,
                        modifier = Modifier.combinedClickable(
                            role = Role.Button,
                            onClick = { onEdit(entry) },
                            onLongClick = { menu = true },
                        ),
                    )
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { AppText("Edit", size = 15, color = FinoraColors.TextPrimary) },
                            leadingIcon = { Icon(sfSymbolIcon("pencil"), null, tint = FinoraColors.TextPrimary) },
                            onClick = { menu = false; onEdit(entry) },
                        )
                        DropdownMenuItem(
                            text = { AppText("Delete", size = 15, color = FinoraColors.Negative) },
                            leadingIcon = { Icon(sfSymbolIcon("trash"), null, tint = FinoraColors.Negative) },
                            onClick = { menu = false; onDelete(entry) },
                        )
                    }
                }
                if (index != entries.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
            }
            AppText(
                "Tap an entry to edit it, touch and hold to delete.",
                size = 11, weight = AppFontWeight.Medium, color = FinoraColors.TextTertiary,
            )
        }
    }
}
