package com.shameem.finora.screens.categories.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.categories.model.CategoryKind
import com.shameem.finora.screens.categories.model.TransactionCategory

/** Rounded tinted square holding the category's symbol (iOS `CategoryIconBadge`). */
@Composable
fun CategoryIconBadge(
    icon: String,
    tint: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val color = remember(tint) { colorFromHex(tint) }
    Box(
        modifier = modifier
            .size(size)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = sfSymbolIcon(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.45f),
        )
    }
}

/** One category in the list, also used as the live preview in the form (iOS `CategoryRow`). */
@Composable
fun CategoryRow(
    category: TransactionCategory,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .finoraCard()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CategoryIconBadge(icon = category.icon, tint = category.tint)

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            ShrinkToFitText(
                text = category.name,
                style = appTextStyle(size = 16, weight = AppFontWeight.Bold),
                color = FinoraColors.TextPrimary,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
            if (category.kind == CategoryKind.Expense) {
                AppText(
                    text = category.monthlyBudget
                        ?.let { "Budget ${CurrencyFormatter.taka(it, showFraction = false)} / month" }
                        ?: "No budget set",
                    size = 12,
                    weight = AppFontWeight.Medium,
                    color = FinoraColors.TextSecondary,
                    maxLines = 1,
                )
            }
        }

        Icon(
            imageVector = sfSymbolIcon("chevron.right"),
            contentDescription = null,
            tint = FinoraColors.TextTertiary,
            modifier = Modifier.size(16.dp),
        )
    }
}
