package com.shameem.finora.core.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.util.TakaFormatter
import kotlin.math.roundToInt

/** One legend/segment entry. [name] is the stable identity so the chart animates between updates. */
data class BreakdownItem(
    val name: String,
    val amount: Double,
    val color: Color,
    /** SF Symbol name (resolved with [sfSymbolIcon]). */
    val icon: String,
) {
    val id: String get() = name
}

/** Icon, name, amount and a share-of-total bar (iOS `BreakdownRow`). */
@Composable
fun BreakdownRow(
    item: BreakdownItem,
    fraction: Double,
    modifier: Modifier = Modifier,
) {
    val animatedFraction by animateFloatAsState(fraction.toFloat(), label = "breakdown-fraction")
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(item.color.copy(alpha = 0.14f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sfSymbolIcon(item.icon), contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AppText(
                    text = item.name,
                    size = 16,
                    weight = AppFontWeight.Bold,
                    color = FinoraColors.TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false),
                )
                AppText(
                    text = TakaFormatter.string(item.amount),
                    size = 16,
                    weight = AppFontWeight.ExtraBold,
                    color = FinoraColors.TextPrimary,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProgressBar(fraction = animatedFraction.toDouble(), color = item.color, modifier = Modifier.weight(1f))
                AppText(
                    text = "${(fraction * 100).roundToInt()}%",
                    size = 12,
                    weight = AppFontWeight.SemiBold,
                    color = FinoraColors.TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(34.dp),
                )
            }
        }
    }
}

/**
 * Donut chart with total in the middle and a per-item breakdown below (iOS `DonutCard`).
 * The optional [title] adds a header, with [subtitle] as a small capsule beside it.
 */
@Composable
fun DonutCard(
    centerCaption: String,
    items: List<BreakdownItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
) {
    val total = remember(items) { items.sumOf { it.amount } }
    val slices = remember(items) { items.map { DonutSlice(it.id, it.amount, it.color) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(20.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (title != null) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppText(
                    text = title,
                    size = 18,
                    weight = AppFontWeight.Bold,
                    color = FinoraColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                if (subtitle != null) {
                    AppText(
                        text = subtitle,
                        size = 13,
                        weight = AppFontWeight.Medium,
                        color = FinoraColors.TextSecondary,
                        modifier = Modifier
                            .background(FinoraColors.FillSubtle, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }
            }
        }

        DonutChart(slices = slices, modifier = Modifier.fillMaxWidth().height(230.dp)) {
            Column(
                modifier = Modifier.padding(horizontal = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppText(
                    text = centerCaption.uppercase(),
                    size = 13,
                    weight = AppFontWeight.Medium,
                    color = FinoraColors.TextSecondary,
                    letterSpacing = 1.sp,
                )
                ShrinkToFitText(
                    text = TakaFormatter.string(total),
                    style = appTextStyle(size = 32, weight = AppFontWeight.ExtraBold),
                    color = FinoraColors.TextPrimary,
                    minScale = 0.5f,
                    alignment = Alignment.Center,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        HorizontalDivider(color = FinoraColors.Divider)

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items.forEach { item ->
                BreakdownRow(item = item, fraction = if (total > 0) item.amount / total else 0.0)
            }
        }
    }
}
