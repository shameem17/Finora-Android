package com.shameem.finora.screens.loan.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.core.util.ReportPeriod
import com.shameem.finora.screens.loan.model.Loan
import com.shameem.finora.screens.loan.model.LoanSummary
import kotlin.math.abs

@Composable
fun LoanSummaryCard(period: ReportPeriod, summary: LoanSummary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Header(period)
        NetPosition(summary)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BalanceTile(
                title = "To receive",
                value = summary.toReceive,
                caption = countCaption(summary.activeLentCount),
                icon = "arrow.down.left",
                tint = FinoraColors.Positive,
                modifier = Modifier.weight(1f),
            )
            BalanceTile(
                title = "To pay",
                value = summary.toPay,
                caption = countCaption(summary.activeBorrowedCount),
                icon = "arrow.up.right",
                tint = FinoraColors.Negative,
                modifier = Modifier.weight(1f),
            )
        }
        HorizontalDivider(color = FinoraColors.Divider)
        TileLabel("Activity ${period.phrase}")
        TileGrid(
            listOf(
                StatTileData("Lent out", CurrencyFormatter.taka(summary.lentInPeriod), "arrow.up.right", FinoraColors.BrandSecondary),
                StatTileData("Borrowed", CurrencyFormatter.taka(summary.borrowedInPeriod), "arrow.down.left", FinoraColors.BrandSecondary),
                StatTileData("Received back", CurrencyFormatter.taka(summary.receivedInPeriod), "checkmark.circle", FinoraColors.Positive),
                StatTileData("Paid back", CurrencyFormatter.taka(summary.repaidInPeriod), "checkmark.circle", FinoraColors.BrandSecondary),
            ),
        )
        TileLabel(if (period == ReportPeriod.All) "Scheduled in total" else "Due ${period.phrase}")
        TileGrid(
            listOf(
                StatTileData(
                    "You need to pay", CurrencyFormatter.taka(summary.dueToPay), "calendar.badge.clock",
                    FinoraColors.Negative, overdueCaption(summary.overdueToPay),
                ),
                StatTileData(
                    "You'll receive", CurrencyFormatter.taka(summary.dueToReceive), "calendar.badge.checkmark",
                    FinoraColors.Positive, overdueCaption(summary.overdueToReceive),
                ),
            ),
        )
    }
}

@Composable
private fun Header(period: ReportPeriod) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppText("Loan Overview", size = 18, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, modifier = Modifier.weight(1f))
        AppText(
            period.subtitle(),
            size = 13,
            weight = AppFontWeight.Medium,
            color = FinoraColors.TextSecondary,
            maxLines = 1,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(FinoraColors.FillSubtle)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun NetPosition(summary: LoanSummary) {
    val isSquare = abs(summary.net) < Loan.TOLERANCE
    val amount = CurrencyFormatter.taka(abs(summary.net))
    val text = if (isSquare) amount else (if (summary.net > 0) "+" else "−") + amount
    val color = when {
        isSquare -> FinoraColors.TextPrimary
        summary.net > 0 -> FinoraColors.Positive
        else -> FinoraColors.Negative
    }
    val caption = when {
        isSquare -> "You're all square."
        summary.net > 0 -> "You're owed more than you owe."
        else -> "You owe more than you're owed."
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TileLabel("NET POSITION")
        ShrinkToFitText(
            text = text,
            style = appTextStyle(size = 40, weight = AppFontWeight.ExtraBold),
            color = color,
            minScale = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(caption, size = 13, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
    }
}

@Composable
private fun TileLabel(text: String) {
    AppText(
        text = text.uppercase(),
        size = 12,
        weight = AppFontWeight.SemiBold,
        color = FinoraColors.TextSecondary,
        letterSpacing = 1.sp,
        maxLines = 1,
    )
}

private fun countCaption(count: Int): String = when (count) {
    0 -> "Nothing open"
    1 -> "1 open entry"
    else -> "$count open entries"
}

private fun overdueCaption(amount: Double): String? =
    if (amount >= Loan.TOLERANCE) "${CurrencyFormatter.taka(amount)} overdue" else null

@Composable
private fun BalanceTile(
    title: String,
    value: Double,
    caption: String,
    icon: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(tint.copy(alpha = 0.07f))
            .border(BorderStroke(1.dp, tint.copy(alpha = 0.15f)), shape)
            .padding(14.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconChip(icon, tint, size = 28, corner = 9, glyph = 14)
            AppText(title, size = 13, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary, maxLines = 1)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ShrinkToFitText(
                text = CurrencyFormatter.taka(value),
                style = appTextStyle(size = 22, weight = AppFontWeight.ExtraBold),
                color = FinoraColors.TextPrimary,
                minScale = 0.6f,
                modifier = Modifier.fillMaxWidth(),
            )
            AppText(caption, size = 11, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
        }
    }
}

private data class StatTileData(
    val title: String,
    val value: String,
    val icon: String,
    val tint: Color,
    val caption: String? = null,
)

/** Two tiles per row with equal widths, top-aligned (iOS `LazyVGrid` with two flexible columns). ShrinkToFitText can't be measured intrinsically, so heights aren't equalised. */
@Composable
private fun TileGrid(tiles: List<StatTileData>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                row.forEach { StatTile(it, Modifier.weight(1f)) }
                if (row.size == 1) Box(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(data: StatTileData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FinoraColors.FillSubtle)
            .padding(12.dp)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconChip(data.icon, data.tint, size = 34, corner = 10, glyph = 16)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(data.title, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
            ShrinkToFitText(
                text = data.value,
                style = appTextStyle(size = 15, weight = AppFontWeight.ExtraBold),
                color = FinoraColors.TextPrimary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
            data.caption?.let {
                AppText(it, size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.Negative, maxLines = 1)
            }
        }
    }
}

@Composable
private fun IconChip(icon: String, tint: Color, size: Int, corner: Int, glyph: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(corner.dp))
            .background(tint.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(sfSymbolIcon(icon), contentDescription = null, tint = tint, modifier = Modifier.size(glyph.dp))
    }
}
