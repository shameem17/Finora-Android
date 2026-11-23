package com.shameem.finora.screens.loan.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.CustomColor
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ProgressBar
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanItem
import com.shameem.finora.screens.loan.model.LoanKind
import com.shameem.finora.screens.loan.model.LoanStatus
import com.shameem.finora.screens.loan.model.Person
import com.shameem.finora.screens.loan.model.PersonCategory
import com.shameem.finora.services.date.shortDisplay
import kotlin.math.roundToInt

// region Kind copy (iOS `LoanKind` extension)

val LoanKind.tint: CustomColor get() = CustomColor.Accent
val LoanKind.personRole: String get() = if (this == LoanKind.Lent) "Borrower" else "Lender"
val LoanKind.addTitle: String get() = if (this == LoanKind.Lent) "Add New Loan" else "Add New Borrow"
val LoanKind.accountFieldTitle: String get() = if (this == LoanKind.Lent) "Paid from" else "Received in"
val LoanKind.repaymentAccountTitle: String get() = if (this == LoanKind.Lent) "Received in" else "Paid from"
val LoanKind.repaymentActionTitle: String get() = if (this == LoanKind.Lent) "Record Received" else "Record Payment"
val LoanKind.repaymentsHeading: String get() = if (this == LoanKind.Lent) "Repayments received" else "Payments made"
val LoanKind.outstandingCaption: String get() = if (this == LoanKind.Lent) "Still to receive" else "Still to pay"
val LoanKind.typeDescription: String get() = if (this == LoanKind.Lent) "Loan given" else "Money borrowed"
val LoanKind.emptyTitle: String get() = if (this == LoanKind.Lent) "No loans yet" else "No borrowings yet"
val LoanKind.emptyMessage: String
    get() = if (this == LoanKind.Lent) {
        "Money you lend to friends, family or others will be tracked here."
    } else {
        "Money you borrow from people, banks or wallets will be tracked here."
    }

// endregion

// region Status (iOS `LoanStatus` extension)

val LoanStatus.label: String?
    get() = when (this) {
        LoanStatus.Settled -> "Settled"
        is LoanStatus.Overdue -> if (days == 1) "Overdue 1 day" else "Overdue $days days"
        is LoanStatus.Due -> when (inDays) {
            0 -> "Due today"
            1 -> "Due tomorrow"
            else -> "Due in $inDays days"
        }
        LoanStatus.Open -> null
    }

val LoanStatus.color: Color
    get() = when (this) {
        LoanStatus.Settled -> FinoraColors.Positive
        is LoanStatus.Overdue -> FinoraColors.Negative
        is LoanStatus.Due -> if (inDays <= 7) FinoraColors.TextPrimary else FinoraColors.TextSecondary
        LoanStatus.Open -> FinoraColors.TextSecondary
    }

// endregion

/** "this week", "this month"… used in card captions (iOS `ReportPeriod.phrase`). */
val ReportPeriod.phrase: String
    get() = when (this) {
        ReportPeriod.Week -> "this week"
        ReportPeriod.Month -> "this month"
        ReportPeriod.Year -> "this year"
        ReportPeriod.All -> "all time"
    }

fun Loan.progressPercent(): Int = (progress * 100).roundToInt()

/** Circle with the person's initials, or a category glyph for banks/wallets (iOS `PersonAvatar`). */
@Composable
fun PersonAvatar(person: Person, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    val tint = FinoraColors.BrandSecondary
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        if (person.category == PersonCategory.Individual) {
            AppText(person.initials, size = (size.value * 0.36f).roundToInt(), weight = AppFontWeight.ExtraBold, color = tint)
        } else {
            Icon(
                imageVector = sfSymbolIcon(person.category.icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * 0.42f),
            )
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    AppText(
        text = text,
        size = 11,
        weight = AppFontWeight.Bold,
        color = color,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun LoanRow(item: LoanItem, modifier: Modifier = Modifier) {
    val loan = item.loan
    val isSettled = item.status == LoanStatus.Settled
    val showsProgress = !isSettled && loan.totalRepaid > 0
    val subtitle = "${loan.account.name} · ${loan.date.shortDisplay()}"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PersonAvatar(item.person, size = 46.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(item.person.name, size = 15, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
                AppText(subtitle, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppText(
                    CurrencyFormatter.taka(if (isSettled) loan.totalPayable else loan.outstanding, showFraction = true),
                    size = 15,
                    weight = AppFontWeight.ExtraBold,
                    color = FinoraColors.TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.alpha(if (isSettled) 0.5f else 1f),
                )
                item.status.label?.let { StatusChip(it, item.status.color) }
            }
            Icon(
                imageVector = sfSymbolIcon("chevron.right"),
                contentDescription = null,
                tint = FinoraColors.TextTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        if (showsProgress) {
            Row(
                modifier = Modifier.padding(start = 60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProgressBar(fraction = loan.progress, color = loan.kind.tint.color, modifier = Modifier.weight(1f))
                AppText("${loan.progressPercent()}% repaid", size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun LoanEmptyState(kind: LoanKind, isSearching: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = sfSymbolIcon(if (isSearching) "magnifyingglass" else "banknote"),
            contentDescription = null,
            tint = FinoraColors.TextTertiary,
            modifier = Modifier.size(44.dp),
        )
        AppText(
            if (isSearching) "No matches" else kind.emptyTitle,
            size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary,
        )
        AppText(
            if (isSearching) "Try a different name or note." else kind.emptyMessage,
            size = 14, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, textAlign = TextAlign.Center,
        )
    }
}
