package com.shameem.finora.screens.loan.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AddActionButton
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ConfirmDeleteDialog
import com.shameem.finora.core.ui.FormErrorBanner
import com.shameem.finora.core.ui.FormFieldRow
import com.shameem.finora.core.ui.FormPrimaryButton
import com.shameem.finora.core.ui.FormSheetHost
import com.shameem.finora.core.ui.FormTextEntry
import com.shameem.finora.core.ui.SearchField
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.model.PersonCategory
import com.shameem.finora.screens.loan.viewmodel.LoanFormViewModel
import com.shameem.finora.screens.loan.viewmodel.PeopleViewModel
import com.shameem.finora.screens.loan.viewmodel.PeopleViewModelProtocol
import com.shameem.finora.screens.loan.viewmodel.PersonFormViewModel
import com.shameem.finora.screens.loan.viewmodel.PersonRowData
import com.shameem.finora.screens.loan.viewmodel.PhoneInput
import kotlinx.coroutines.launch
import kotlin.math.abs

/** "Manage People" sheet: the list of people with a search field and an add button (iOS `PeopleView`). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleSheet(model: PeopleViewModel, onDismiss: () -> Unit) {
    PeopleView(model, onDismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleView(model: PeopleViewModelProtocol, onDismiss: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var form by remember { mutableStateOf<PersonFormViewModel?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText("People", size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary) },
                actions = {
                    TextButton(onClick = onDismiss) {
                        AppText("Done", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinoraColors.ScreenBackground),
            )
        },
        bottomBar = {
            AddActionButton(title = "Add Person", onClick = { scope.launch { form = model.makeFormViewModel(null) } })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!state.isEmpty) {
                SearchField(state.searchText, model::setSearchText, prompt = "Search by name or number")
            }
            if (state.rows.isEmpty()) {
                PeopleEmptyState(isSearching = !state.isEmpty)
            } else {
                Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(horizontal = 16.dp)) {
                    state.rows.forEachIndexed { index, row ->
                        PersonListRow(row, Modifier.clickable(role = Role.Button) {
                            scope.launch { form = model.makeFormViewModel(row.person) }
                        })
                        if (index != state.rows.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
                    }
                }
            }
        }
    }

    form?.let { current ->
        FormSheetHost(onDismissRequest = { form = null }) { PersonFormView(current, onDismiss = { form = null }) }
    }
}

@Composable
private fun PersonListRow(row: PersonRowData, modifier: Modifier = Modifier) {
    val person = row.person
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PersonAvatar(person, size = 46.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(person.name, size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
            AppText(
                person.phone?.takeIf { it.isNotEmpty() } ?: person.category.title,
                size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (abs(row.netBalance) < Loan.TOLERANCE) {
                AppText(
                    if (row.totalCount == 0) "No entries" else "Settled",
                    size = 13, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                )
            } else {
                AppText(
                    CurrencyFormatter.taka(abs(row.netBalance), showFraction = true),
                    size = 15,
                    weight = AppFontWeight.ExtraBold,
                    color = if (row.netBalance > 0) FinoraColors.Positive else FinoraColors.Negative,
                )
                AppText(
                    if (row.netBalance > 0) "owes you" else "you owe",
                    size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                )
            }
        }
        Icon(sfSymbolIcon("chevron.right"), null, tint = FinoraColors.TextTertiary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun PeopleEmptyState(isSearching: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().finoraCard().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            sfSymbolIcon(if (isSearching) "magnifyingglass" else "person.2"),
            contentDescription = null,
            tint = FinoraColors.TextTertiary,
            modifier = Modifier.size(44.dp),
        )
        AppText(if (isSearching) "No matches" else "No people yet", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
        AppText(
            if (isSearching) "Try a different name or number."
            else "Add friends, family, banks or wallets here so you can pick them quickly when you lend or borrow.",
            size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, textAlign = TextAlign.Center,
        )
    }
}

/**
 * Add / edit a person (iOS `PersonFormView`). When [isEmbedded] (pushed inside the person picker) the left
 * action is a back arrow instead of Cancel; either way it calls [onDismiss].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonFormView(form: PersonFormViewModel, onDismiss: () -> Unit, isEmbedded: Boolean = false) {
    val focusManager = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText(form.title, size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1) },
                navigationIcon = {
                    if (isEmbedded) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FinoraColors.TextPrimary)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            AppText("Cancel", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinoraColors.ScreenBackground),
            )
        },
        bottomBar = {
            FormPrimaryButton(title = "Save Person", isEnabled = form.canSubmit, onClick = {
                focusManager.clearFocus()
                if (form.submit()) onDismiss()
            })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IdentityCard(form, nameFocus)
            ContactCard(form)
            FormErrorBanner(message = form.duplicateError)
            if (form.isEditing) DeleteSection(form, onDelete = { confirmDelete = true })
        }
    }

    if (confirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete this person?",
            message = "They'll be removed from your people list.",
            onConfirm = {
                confirmDelete = false
                if (form.delete()) onDismiss()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun IdentityCard(form: PersonFormViewModel, nameFocus: FocusRequester) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionLabel("Name")
        Box(Modifier.fillMaxWidth()) {
            if (form.name.isEmpty()) {
                AppText("e.g. Rahim Uddin", size = 22, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextTertiary)
            }
            androidx.compose.foundation.text.BasicTextField(
                value = form.name,
                onValueChange = { form.name = it },
                singleLine = true,
                textStyle = com.shameem.finora.core.designsystem.appTextStyle(size = 22, weight = AppFontWeight.ExtraBold)
                    .copy(color = FinoraColors.TextPrimary),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(FinoraColors.TextPrimary),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(nameFocus),
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        SectionLabel("Type")
        // Two chips per row (iOS two-column LazyVGrid).
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PersonCategory.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { category ->
                        CategoryChip(category, form.category == category, Modifier.weight(1f)) { form.category = category }
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(category: PersonCategory, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val fg = if (isSelected) FinoraColors.ScreenBackground else FinoraColors.TextSecondary
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) FinoraColors.TextPrimary else FinoraColors.FillSubtle)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(sfSymbolIcon(category.icon), null, tint = fg, modifier = Modifier.size(15.dp))
        AppText(category.title, size = 14, weight = AppFontWeight.Bold, color = fg, maxLines = 1)
    }
}

@Composable
private fun ContactCard(form: PersonFormViewModel) {
    Column(Modifier.fillMaxWidth().finoraCard().padding(horizontal = 16.dp)) {
        FormFieldRow(icon = "phone.fill", title = "Contact no. (optional)") {
            FormTextEntry(
                value = form.phone,
                onValueChange = { form.phone = PhoneInput.sanitize(it) },
                placeholder = "01XXXXXXXXX",
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.width(170.dp),
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        FormFieldRow(icon = "mappin.and.ellipse", title = "Address (optional)", alignTop = true) {
            FormTextEntry(
                value = form.address,
                onValueChange = { form.address = it },
                placeholder = "Area, city…",
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                maxLines = 3,
            )
        }
    }
}

@Composable
private fun DeleteSection(form: PersonFormViewModel, onDelete: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (form.canDelete) 1f else 0.45f)
                .clip(CircleShape)
                .background(FinoraColors.Negative.copy(alpha = 0.1f))
                .clickable(enabled = form.canDelete, role = Role.Button, onClick = onDelete)
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(sfSymbolIcon("trash"), null, tint = FinoraColors.Negative, modifier = Modifier.size(18.dp))
            AppText("Delete Person", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.Negative)
        }
        form.deleteBlockedReason?.let {
            AppText(
                it, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp),
            )
        }
    }
}

/** Pick (or add) the borrower / lender for a loan (iOS `PersonPickerView`). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonPickerView(form: LoanFormViewModel, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var newPerson by remember { mutableStateOf<PersonFormViewModel?>(null) }

    // A newly added person becomes selected (the form VM sets personId); leave the picker once that happens.
    val selectedBefore = remember { form.personId }
    androidx.compose.runtime.LaunchedEffect(form.personId) {
        if (form.personId != selectedBefore) onDismiss()
    }

    val embedded = newPerson
    if (embedded != null) {
        PersonFormView(embedded, onDismiss = { newPerson = null }, isEmbedded = true)
        return
    }

    val needle = search.trim()
    val filtered = if (needle.isEmpty()) form.people else form.people.filter { it.name.contains(needle, ignoreCase = true) }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = FinoraColors.ScreenBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { AppText("Select ${form.kind.personRole}", size = 17, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1) },
                navigationIcon = {
                    TextButton(onClick = onDismiss) {
                        AppText("Cancel", size = 16, weight = AppFontWeight.SemiBold, color = FinoraColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FinoraColors.ScreenBackground),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (form.people.isNotEmpty()) SearchField(search, { search = it }, prompt = "Search")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .finoraCard(cornerRadius = 20.dp)
                    .clickable(role = Role.Button) { scope.launch { newPerson = form.makePersonFormViewModel() } }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape).background(FinoraColors.Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(sfSymbolIcon("plus"), null, tint = FinoraColors.TextPrimary, modifier = Modifier.size(18.dp))
                }
                AppText("Add new person", size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
            }
            if (filtered.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().finoraCard(cornerRadius = 24.dp).padding(horizontal = 16.dp)) {
                    filtered.forEachIndexed { index, person ->
                        PickerRow(person, isSelected = person.id == form.personId) {
                            form.personId = person.id
                            onDismiss()
                        }
                        if (index != filtered.lastIndex) HorizontalDivider(color = FinoraColors.Divider)
                    }
                }
            } else if (form.people.isNotEmpty()) {
                AppText(
                    "No one matches “$search”.",
                    size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PickerRow(person: Person, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PersonAvatar(person, size = 46.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(person.name, size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
            AppText(person.phone ?: person.category.title, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
        }
        if (isSelected) {
            Icon(sfSymbolIcon("checkmark.circle.fill"), "Selected", tint = FinoraColors.Positive)
        }
    }
}
