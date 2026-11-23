package com.shameem.finora.screens.savings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ProgressBar
import com.shameem.finora.core.ui.SectionLabel
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.StatColumn
import com.shameem.finora.core.ui.sectionCard
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.savings.model.SavingsBreakdownRow
import com.shameem.finora.screens.savings.model.SavingsSummary
import com.shameem.finora.screens.transaction.AccountBadge
import kotlin.math.roundToInt

@Composable
fun SavingsSummaryCard(summary: SavingsSummary, modifier: Modifier = Modifier) {
    Column(modifier = modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SectionLabel("Total savings")
            ShrinkToFitText(
                text = CurrencyFormatter.taka(summary.totalSavings, showFraction = true),
                style = appTextStyle(size = 34, weight = AppFontWeight.ExtraBold),
                color = FinoraColors.TextPrimary,
                minScale = 0.5f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        summary.progress?.let { progress ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProgressBar(fraction = progress, color = FinoraColors.BrandSecondary)
                AppText(
                    "${(progress * 100).roundToInt()}% of ${CurrencyFormatter.taka(summary.totalTarget)} target",
                    size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                )
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        Row(Modifier.fillMaxWidth()) {
            StatColumn("Deposited", CurrencyFormatter.taka(summary.totalDeposited), Modifier.weight(1f))
            StatColumn("Withdrawn", CurrencyFormatter.taka(summary.totalWithdrawn), Modifier.weight(1f))
            StatColumn("Goals reached", "${summary.goalsReached}/${summary.count}", Modifier.weight(1f))
        }
    }
}

@Composable
fun SavingsBreakdownCard(rows: List<SavingsBreakdownRow>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.sectionCard(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionLabel("Breakdown by account")
        rows.forEach { row ->
            val tint = remember(row.tint) { colorFromHex(row.tint) }
            Column(
                modifier = Modifier.semantics(mergeDescendants = true) {},
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccountBadge(type = row.type, tint = row.tint, size = 34.dp)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        AppText(row.name, size = 14, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
                        AppText(
                            if (row.count == 1) "1 savings account" else "${row.count} savings accounts",
                            size = 11, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        AppText(
                            CurrencyFormatter.taka(row.amount, showFraction = true),
                            size = 14, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary,
                        )
                        AppText(
                            "${(row.share * 100).roundToInt()}%",
                            size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextSecondary,
                        )
                    }
                }
                ProgressBar(fraction = row.share, color = tint)
            }
        }
    }
}
