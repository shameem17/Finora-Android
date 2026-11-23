package com.shameem.finora.screens.loan.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ConfirmDeleteDialog
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.ProgressBar
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.ValueRow
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.loan.model.Installment
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanItem
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.model.Repayment
import com.shameem.finora.screens.loan.viewmodel.LoanDetailViewModelProtocol
import com.shameem.finora.screens.loan.viewmodel.LoanFormViewModel
import com.shameem.finora.screens.loan.viewmodel.RepaymentFormViewModel
import com.shameem.finora.screens.transaction.AccountBadge
import com.shameem.finora.services.date.shortDisplay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailView(
    model: LoanDetailViewModelProtocol,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var repaymentForm by remember { mutableStateOf<RepaymentFormViewModel?>(null) }
    var editForm by remember { mutableStateOf<LoanFormViewModel?>(null) }
    var confirmDeleteLoan by remember { mutableStateOf(false) }
    var repaymentToDelete by remember { mutableStateOf<Repayment?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(model) { model.load() }

    val item = state.item

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText(item?.person?.name ?: "Details", size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FinoraColors.TextPrimary)
                    }
                },
                actions = {
                    if (item != null) {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(sfSymbolIcon("ellipsis.circle"), contentDescription = "More", tint = FinoraColors.TextPrimary)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { AppText("Edit details", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary) },
                                leadingIcon = { Icon(sfSymbolIcon("pencil"), null, tint = FinoraColors.TextPrimary) },
                                onClick = {
                                    menuOpen = false
                                    scope.launch { editForm = model.makeEditFormViewModel() }
                                },
                            )
                            DropdownMenuItem(
                                text = { AppText("Delete", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.Negative) },
                                leadingIcon = { Icon(sfSymbolIcon("trash"), null, tint = FinoraColors.Negative) },
                                onClick = {
                                    menuOpen = false
                                    confirmDeleteLoan = true
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinoraColors.ScreenBackground),
            )
        },
        bottomBar = {
            if (item != null && !item.loan.isSettled) {
                AddActionButton(
                    title = item.loan.kind.repaymentActionTitle,
                    tint = item.loan.kind.tint,
                    onClick = { scope.launch { repaymentForm = model.makeRepaymentFormViewModel() } },
                )
            }
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
                Header(item)
                Terms(item)
                if (state.installments.size > 1) Schedule(state.installments)
                History(item, state.repayments, onDelete = { repaymentToDelete = it })
            }
        }
    }

    repaymentForm?.let { current ->
        FormSheetHost(onDismissRequest = { repaymentForm = null }) { RepaymentFormView(current, onDismiss = { repaymentForm = null }) }
    }
    editForm?.let { current ->
        FormSheetHost(onDismissRequest = { editForm = null }) { LoanFormView(current, onDismiss = { editForm = null }) }
    }
    if (confirmDeleteLoan) {
        ConfirmDeleteDialog(
            title = "Delete this entry?",
            message = "All of its payments are removed too, and your account balances go back to how they were before it.",
            onConfirm = {
                confirmDeleteLoan = false
                model.deleteLoan()
            },
            onDismiss = { confirmDeleteLoan = false },
        )
    }
    repaymentToDelete?.let { repayment ->
        ConfirmDeleteDialog(
            title = "Delete this payment?",
            message = "${CurrencyFormatter.taka(repayment.amount, showFraction = true)} will go back to the ${repayment.account.name} balance adjustment.",
            onConfirm = {
                repaymentToDelete = null
                model.deleteRepayment(repayment.id)
            },
            onDismiss = { repaymentToDelete = null },
        )
    }
}

@Composable
private fun Header(item: LoanItem) {
    val loan = item.loan
    val person = item.person
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PersonAvatar(person, size = 56.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(person.name, size = 18, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary, maxLines = 1)
                AppText(
                    "${loan.kind.personRole} · ${person.category.title}",
                    size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                )
            }
            item.status.label?.let { StatusChip(it, item.status.color) }
        }
        ContactLines(person)
        HorizontalDivider(color = FinoraColors.Divider)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel(if (loan.isSettled) "Fully settled" else loan.kind.outstandingCaption)
            ShrinkToFitText(
                text = CurrencyFormatter.taka(if (loan.isSettled) loan.totalPayable else loan.outstanding, showFraction = true),
                style = appTextStyle(size = 40, weight = AppFontWeight.ExtraBold),
                color = if (loan.isSettled) FinoraColors.Positive else FinoraColors.TextPrimary,
                minScale = 0.5f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressBar(fraction = loan.progress, color = loan.kind.tint.color)
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    "${if (loan.kind == LoanKind.Lent) "Received" else "Paid"} " +
                        "${CurrencyFormatter.taka(loan.totalRepaid, showFraction = true)} of " +
                        CurrencyFormatter.taka(loan.totalPayable, showFraction = true),
                    size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                AppText("${loan.progressPercent()}%", size = 12, weight = AppFontWeight.Bold, color = FinoraColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun ContactLines(person: Person) {
    val phone = person.phone.orEmpty()
    val address = person.address.orEmpty()
    if (phone.isEmpty() && address.isEmpty()) return
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (phone.isNotEmpty()) {
            val dial = "tel:" + phone.filter { it.isDigit() || it == '+' }
            ContactLine(
                icon = "phone.fill",
                text = phone,
                modifier = Modifier.clickable(role = Role.Button) {
                    runCatching { uriHandler.openUri(dial) } // no dialer on this device: ignore
                },
            )
        }
        if (address.isNotEmpty()) ContactLine(icon = "mappin.and.ellipse", text = address)
    }
}

@Composable
private fun ContactLine(icon: String, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(sfSymbolIcon(icon), null, tint = FinoraColors.TextSecondary, modifier = Modifier.size(15.dp))
        AppText(text, size = 13, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary)
    }
}

@Composable
private fun Terms(item: LoanItem) {
    val loan = item.loan
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ValueRow("Type", loan.kind.typeDescription)
        ValueRow(loan.kind.accountFieldTitle, loan.account.name)
        ValueRow("Date", loan.date.shortDisplay())
        HorizontalDivider(color = FinoraColors.Divider)
        ValueRow("Amount", CurrencyFormatter.taka(loan.principal, showFraction = true))
        if (loan.interest > 0) {
            ValueRow("Interest / extra", CurrencyFormatter.taka(loan.interest, showFraction = true))
            ValueRow("Total", CurrencyFormatter.taka(loan.totalPayable, showFraction = true), emphasized = true)
        }
        HorizontalDivider(color = FinoraColors.Divider)
        val due = loan.dueDate
        if (due != null) {
            ValueRow(if (loan.installmentCount > 1) "First due" else "Due", due.shortDisplay())
            if (loan.installmentCount > 1) ValueRow("Plan", "${loan.installmentCount} monthly installments")
        } else {
            ValueRow("Due", "No due date")
        }
        loan.note?.takeIf { it.isNotEmpty() }?.let {
            HorizontalDivider(color = FinoraColors.Divider)
            ValueRow("Note", it)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    AppText(text, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextSecondary, modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun Schedule(installments: List<Installment>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Installments")
        Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(horizontal = 16.dp)) {
            installments.forEachIndexed { index, installment ->
                InstallmentRow(installment)
                if (index != installments.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
            }
        }
    }
}

@Composable
private fun InstallmentRow(installment: Installment) {
    val today = remember { LocalDate.now() }
    val dueDay = installment.dueDate.atZone(ZoneId.systemDefault()).toLocalDate()
    val isOverdue = !installment.isPaid && dueDay.isBefore(today)
    val icon = when {
        installment.isPaid -> "checkmark.circle.fill"
        isOverdue -> "exclamationmark.circle.fill"
        else -> "circle"
    }
    val color: Color = when {
        installment.isPaid -> FinoraColors.Positive
        isOverdue -> FinoraColors.Negative
        else -> FinoraColors.TextTertiary
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(sfSymbolIcon(icon), null, tint = color, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText("Installment ${installment.number}", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
            AppText(
                installment.dueDate.shortDisplay(),
                size = 12, weight = AppFontWeight.Medium,
                color = if (isOverdue) FinoraColors.Negative else FinoraColors.TextSecondary,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(
                CurrencyFormatter.taka(installment.amount, showFraction = true),
                size = 15, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary,
                modifier = Modifier.alpha(if (installment.isPaid) 0.5f else 1f),
            )
            if (!installment.isPaid && installment.paid > 0) {
                AppText(
                    "${CurrencyFormatter.taka(installment.paid, showFraction = true)} paid",
                    size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.Positive,
                )
            }
        }
    }
}

@Composable
private fun History(item: LoanItem, repayments: List<Repayment>, onDelete: (Repayment) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(item.loan.kind.repaymentsHeading)
        if (repayments.isEmpty()) {
            Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AppText("Nothing recorded yet.", size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
            }
        } else {
            Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(horizontal = 16.dp)) {
                repayments.forEachIndexed { index, repayment ->
                    RepaymentRow(repayment, onDelete = { onDelete(repayment) })
                    if (index != repayments.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
                }
            }
        }
    }
}

@Composable
private fun RepaymentRow(repayment: Repayment, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    val subtitle = listOfNotNull(repayment.account.name, repayment.note?.takeIf { it.isNotEmpty() }).joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AccountBadge(type = repayment.account.type, tint = repayment.account.tint, size = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(repayment.date.shortDisplay(), size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
            AppText(subtitle, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
        }
        AppText(
            CurrencyFormatter.taka(repayment.amount, showFraction = true),
            size = 15, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary,
        )
        Column {
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(32.dp)) {
                Icon(sfSymbolIcon("ellipsis"), contentDescription = "Payment options", tint = FinoraColors.TextTertiary, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { AppText("Delete payment", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.Negative) },
                    leadingIcon = { Icon(sfSymbolIcon("trash"), null, tint = FinoraColors.Negative) },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    },
                )
            }
        }
    }
}
