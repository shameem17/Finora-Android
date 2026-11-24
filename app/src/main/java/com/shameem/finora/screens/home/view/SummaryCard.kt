package com.shameem.finora.screens.home.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.TakaFormatter
import com.shameem.finora.screens.home.model.MonthlyInsights
import com.shameem.finora.screens.home.model.SummaryType
import kotlin.math.abs
import kotlin.math.roundToInt

/** One of the Income / Expense / Balance tiles at the top of Home (iOS `SummaryCard`). */
@Composable
fun SummaryCard(type: SummaryType, amount: String, modifier: Modifier = Modifier) {
    val color = type.brandColor.color
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = Color.Black.copy(alpha = 0.2f), spotColor = Color.Black.copy(alpha = 0.2f))
            .clip(shape)
            .background(FinoraColors.CardSurface)
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.7f))))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.12f)), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier.size(16.dp).clip(RoundedCornerShape(5.dp)).background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(sfSymbolIcon(type.icon), null, tint = color, modifier = Modifier.size(11.dp))
            }
            AppText(type.title, size = 14, weight = AppFontWeight.ExtraBold, color = Color.White.copy(alpha = 0.8f), maxLines = 1)
        }
        AppText(amount, size = 20, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
    }
}

/** Savings rate ring, pace message and three small stats for the month (iOS `InsightsCard`). */
@Composable
fun InsightsCard(insights: MonthlyInsights, modifier: Modifier = Modifier) {
    val ink = FinoraColors.TextPrimary
    val ringColor = when {
        insights.savingsRate >= 0.2 -> FinoraColors.BrandSecondary
        insights.savingsRate >= 0.1 -> FinoraColors.BrandSecondary.copy(alpha = 0.75f)
        else -> FinoraColors.BrandSecondary.copy(alpha = 0.5f)
    }
    Column(
        modifier = modifier.fillMaxWidth().finoraCard().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            SavingsRing(insights.savingsRate, ringColor, Modifier.size(96.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppText("SAVINGS RATE", size = 12, weight = AppFontWeight.SemiBold, color = ink.copy(alpha = 0.55f), letterSpacing = 1.sp)
                AppText(
                    "You've kept ${(insights.savingsRate * 100).roundToInt()}% of your income",
                    size = 16, weight = AppFontWeight.Bold, color = ink,
                )
                PaceLabel(insights.pace)
            }
        }
        HorizontalDivider(color = FinoraColors.Divider)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InsightStat("calendar", "Daily avg", TakaFormatter.string(insights.dailyAverage), FinoraColors.BrandSecondary, Modifier.weight(1f))
            InsightStat(
                insights.topExpense.icon, "Top spend", insights.topExpense.name, insights.topExpense.color, Modifier.weight(1f),
                caption = TakaFormatter.string(insights.topExpense.amount),
            )
            val change = insights.expenseChange
            if (change != null) {
                val isDown = change <= 0 // spending less is good
                InsightStat(
                    if (isDown) "arrow.down.right" else "arrow.up.right", "vs last month",
                    "${(abs(change) * 100).roundToInt()}%",
                    if (isDown) FinoraColors.Positive else FinoraColors.Negative, Modifier.weight(1f),
                )
            } else {
                InsightStat("minus", "vs last month", "—", FinoraColors.TextSecondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PaceLabel(pace: MonthlyInsights.Pace) {
    val (icon, text, color) = when (pace) {
        is MonthlyInsights.Pace.OnTrack ->
            Triple("arrow.up.right.circle.fill", "On track to save ${TakaFormatter.string(pace.surplus)}", FinoraColors.Positive)
        is MonthlyInsights.Pace.Overspending ->
            Triple("exclamationmark.triangle.fill", "Projected to overspend by ${TakaFormatter.string(pace.deficit)}", FinoraColors.Negative)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(sfSymbolIcon(icon), null, tint = color, modifier = Modifier.size(15.dp))
        AppText(text, size = 13, weight = AppFontWeight.SemiBold, color = color)
    }
}

@Composable
private fun SavingsRing(progress: Double, color: Color, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress.toFloat().coerceIn(0f, 1f), label = "savings-ring")
    val percent = (progress * 100).roundToInt()
    Box(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = "Savings rate, $percent percent" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.matchParentSize()) {
            val strokeWidth = 10.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(color.copy(alpha = 0.15f), 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(strokeWidth))
            drawArc(color, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }
        AppText("$percent%", size = 20, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary)
    }
}

@Composable
private fun InsightStat(
    icon: String,
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.07f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sfSymbolIcon(icon), null, tint = tint, modifier = Modifier.size(14.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(title, size = 11, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
            AppText(value, size = 15, weight = AppFontWeight.ExtraBold, color = FinoraColors.TextPrimary, maxLines = 1)
            AppText(caption ?: " ", size = 11, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1)
        }
    }
}
