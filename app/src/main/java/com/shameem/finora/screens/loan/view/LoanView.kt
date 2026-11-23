package com.shameem.finora.screens.loan.view

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.PillPicker
import com.shameem.finora.core.ui.ScreenTitle
import com.shameem.finora.core.ui.SearchField
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.LoanSection
import com.shameem.finora.screens.loan.viewmodel.LoanFormViewModel
import com.shameem.finora.screens.loan.viewmodel.LoanViewModelProtocol
import com.shameem.finora.screens.loan.viewmodel.PeopleViewModel
import com.shameem.finora.screens.loan.viewmodel.PersonFormViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LoanView(
    model: LoanViewModelProtocol,
    onShowDetail: (UUID) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by model.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf<LoanFormViewModel?>(null) }
    var people by remember { mutableStateOf<PeopleViewModel?>(null) }
    var personForm by remember { mutableStateOf<PersonFormViewModel?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(model) { model.load() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { AddActionButton(title = state.kind.addTitle, onClick = { form = model.makeFormViewModel() }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ScreenTitle("Loans", Modifier.weight(1f))
                Column(Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(sfSymbolIcon("person.2.fill"), contentDescription = "People", tint = FinoraColors.TextPrimary)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { AppText("Add Person", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary) },
                            leadingIcon = { Icon(sfSymbolIcon("person.badge.plus"), null, tint = FinoraColors.TextPrimary) },
                            onClick = {
                                menuOpen = false
                                scope.launch { personForm = model.makePersonFormViewModel() }
                            },
                        )
                        DropdownMenuItem(
                            text = { AppText("Manage People", size = 15, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary) },
                            leadingIcon = { Icon(sfSymbolIcon("person.2"), null, tint = FinoraColors.TextPrimary) },
                            onClick = {
                                menuOpen = false
                                people = model.makePeopleViewModel()
                            },
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PillPicker(ReportPeriod.entries, state.period, model::setPeriod)
                LoanSummaryCard(period = state.period, summary = state.summary)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PillPicker(LoanKind.entries, state.kind, model::setKind)
                    if (!state.isEmpty) {
                        SearchField(
                            text = state.searchText,
                            onTextChange = model::setSearchText,
                            prompt = "Search by name or note",
                        )
                    }
                    if (state.sections.isEmpty()) {
                        LoanEmptyState(state.kind, isSearching = !state.isEmpty && state.searchText.isNotEmpty())
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            state.sections.forEach { section -> LoanSectionView(section, onShowDetail) }
                        }
                    }
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) { LoanFormView(current, onDismiss = { form = null }) }
    }
    personForm?.let { current ->
        FormSheetHost(onDismissRequest = { personForm = null }) { PersonFormView(current, onDismiss = { personForm = null }) }
    }
    people?.let { current ->
        FormSheetHost(onDismissRequest = { people = null }) { PeopleSheet(current, onDismiss = { people = null }) }
    }
}

@Composable
private fun LoanSectionView(section: LoanSection, onShowDetail: (UUID) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AppText(
                section.group.title,
                size = 16,
                weight = AppFontWeight.Bold,
                color = if (section.group == LoanSection.Group.Overdue) FinoraColors.Negative else FinoraColors.TextSecondary,
            )
            AppText(
                "${section.items.size}",
                size = 12,
                weight = AppFontWeight.Bold,
                color = FinoraColors.TextSecondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FinoraColors.FillSubtle)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
        Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(horizontal = 16.dp)) {
            section.items.forEachIndexed { index, item ->
                LoanRow(item, Modifier.clickable(role = Role.Button) { onShowDetail(item.id) })
                if (index != section.items.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
            }
        }
    }
}
