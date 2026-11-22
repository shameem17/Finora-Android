package com.shameem.finora.screens.transaction

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.EmptyStateCard
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.home.view.AccountType

/** Coloured rounded square showing an account type's icon (iOS `AccountBadge`). */
@Composable
fun AccountBadge(
    type: AccountType,
    tint: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(colorFromHex(tint), RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = sfSymbolIcon(type.symbol),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(size * 0.42f),
        )
    }
}

/** Period total, charges and count (iOS `TransferSummaryCard`). */
@Composable
fun TransferSummaryCard(
    period: TransferPeriod,
    summary: TransferSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(20.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AppText(
                text = "Total Transferred",
                size = 18,
                weight = AppFontWeight.Bold,
                color = FinoraColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            AppText(
                text = period.subtitle(),
                size = 13,
                weight = AppFontWeight.Medium,
                color = FinoraColors.TextSecondary,
                modifier = Modifier
                    .background(FinoraColors.FillSubtle, CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }

        ShrinkToFitText(
            text = CurrencyFormatter.taka(summary.totalAmount),
            style = appTextStyle(size = 40, weight = AppFontWeight.ExtraBold),
            color = FinoraColors.TextPrimary,
            minScale = 0.5f,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider(color = FinoraColors.Divider)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                title = "Charges paid",
                value = CurrencyFormatter.taka(summary.totalCharges, showFraction = true),
                icon = "percent",
                tint = FinoraColors.BrandSecondary,
                modifier = Modifier.weight(1f),
            )
            StatTile(
                title = "Transfers",
                value = summary.count.toString(),
                icon = "arrow.left.arrow.right",
                tint = FinoraColors.BrandSecondary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatTile(
    title: String,
    value: String,
    icon: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(FinoraColors.FillSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(tint.copy(alpha = 0.14f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sfSymbolIcon(icon), contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(text = title, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary)
            ShrinkToFitText(
                text = value,
                style = appTextStyle(size = 15, weight = AppFontWeight.ExtraBold),
                color = FinoraColors.TextPrimary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** One transfer in the history list (iOS `TransferRow`). */
@Composable
fun TransferRow(
    transfer: Transfer,
    modifier: Modifier = Modifier,
) {
    val description = "${transfer.from.name} to ${transfer.to.name}, " +
        CurrencyFormatter.taka(transfer.amount, showFraction = true)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(46.dp)) {
            AccountBadge(type = transfer.from.type, tint = transfer.from.tint, size = 30.dp)
            AccountBadge(
                type = transfer.to.type,
                tint = transfer.to.tint,
                size = 30.dp,
                modifier = Modifier
                    .offset(x = 14.dp, y = 14.dp)
                    .border(2.dp, FinoraColors.CardSurface, RoundedCornerShape(9.dp)),
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            ShrinkToFitText(
                text = "${transfer.from.name} → ${transfer.to.name}",
                style = appTextStyle(size = 15, weight = AppFontWeight.Bold),
                color = FinoraColors.TextPrimary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
            ShrinkToFitText(
                text = transfer.reason.ifEmpty { "Balance transfer" },
                style = appTextStyle(size = 12, weight = AppFontWeight.Medium),
                color = FinoraColors.TextSecondary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(
                text = CurrencyFormatter.taka(transfer.amount, showFraction = true),
                size = 15,
                weight = AppFontWeight.ExtraBold,
                color = FinoraColors.TextPrimary,
                maxLines = 1,
            )
            if (transfer.charge > 0) {
                AppText(
                    text = "+${CurrencyFormatter.taka(transfer.charge, showFraction = true)} fee",
                    size = 11,
                    weight = AppFontWeight.SemiBold,
                    color = FinoraColors.TextSecondary,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/** Shown when no transfer matches the selected period (iOS `EmptyTransfersView`). */
@Composable
fun EmptyTransfersView(period: TransferPeriod, modifier: Modifier = Modifier) {
    EmptyStateCard(
        icon = "arrow.left.arrow.right.circle",
        title = "No transfers yet",
        message = if (period == TransferPeriod.All) {
            "Move money between your accounts and it will show up here."
        } else {
            "Nothing recorded for this period."
        },
        modifier = modifier,
    )
}
