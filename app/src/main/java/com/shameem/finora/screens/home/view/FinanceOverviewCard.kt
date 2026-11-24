package com.shameem.finora.screens.home.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.heroCard
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.home.model.FinanceOverview
import kotlin.math.abs

/** Teal "Net worth" card with accounts, loans and savings tiles (iOS `FinanceOverviewCard`). */
@Composable
fun FinanceOverviewCard(overview: FinanceOverview, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().heroCard().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AppText(
                "NET WORTH", size = 12, weight = AppFontWeight.SemiBold,
                color = FinoraColors.OnHeroSecondary, letterSpacing = 1.sp,
            )
            ShrinkToFitText(
                text = CurrencyFormatter.taka(overview.netWorth),
                style = appTextStyle(size = 34, weight = AppFontWeight.ExtraBold),
                color = FinoraColors.OnHero,
                minScale = 0.5f,
                modifier = Modifier.fillMaxWidth(),
            )
            AppText(
                "Accounts + to collect − to repay", size = 12, weight = AppFontWeight.Medium,
                color = FinoraColors.OnHeroSecondary, maxLines = 1,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile("building.columns.fill", "Accounts", overview.accountsTotal, count(overview.accountCount, "account"), Modifier.weight(1f))
                Tile("arrow.down.left", "To collect", overview.toCollect, "Lent, remaining", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile("arrow.up.right", "To repay", overview.toRepay, "Borrowed, remaining", Modifier.weight(1f))
                Tile("chart.line.uptrend.xyaxis", "Savings", overview.savings, count(overview.savingsCount, "goal"), Modifier.weight(1f))
            }
        }
        HorizontalDivider(color = FinoraColors.OnHeroSecondary.copy(alpha = 0.25f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(sfSymbolIcon("calendar"), null, tint = FinoraColors.OnHeroSecondary, modifier = Modifier.size(15.dp))
                AppText("This month", size = 13, weight = AppFontWeight.SemiBold, color = FinoraColors.OnHeroSecondary)
            }
            AppText(signed(overview.monthBalance), size = 16, weight = AppFontWeight.Bold, color = FinoraColors.OnHero)
        }
    }
}

@Composable
private fun Tile(icon: String, title: String, value: Double, caption: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(FinoraColors.OnHero.copy(alpha = 0.08f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(FinoraColors.Accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(sfSymbolIcon(icon), null, tint = FinoraColors.OnAccent, modifier = Modifier.size(13.dp))
            }
            AppText(title, size = 13, weight = AppFontWeight.SemiBold, color = FinoraColors.OnHeroSecondary, maxLines = 1)
        }
        ShrinkToFitText(
            text = CurrencyFormatter.taka(value),
            style = appTextStyle(size = 18, weight = AppFontWeight.Bold),
            color = FinoraColors.OnHero,
            minScale = 0.6f,
            modifier = Modifier.fillMaxWidth(),
        )
        AppText(caption, size = 11, weight = AppFontWeight.Medium, color = FinoraColors.OnHeroSecondary, maxLines = 1)
    }
}

private fun signed(value: Double): String {
    val sign = if (value > 0) "+" else if (value < 0) "−" else ""
    return sign + CurrencyFormatter.taka(abs(value))
}

private fun count(n: Int, noun: String): String = if (n == 1) "1 $noun" else "$n ${noun}s"
