package com.shameem.finora.screens.savings.view

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.FinoraColors
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ProgressBar
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.finoraCard
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.savings.model.SavingsEntry
import com.shameem.finora.screens.savings.model.SavingsEntryKind
import com.shameem.finora.screens.savings.model.SavingsItem
import com.shameem.finora.services.date.shortDisplay
import kotlin.math.roundToInt

/** Pot logo: the user's image, or a tinted shield (iOS `SavingsLogo`). */
@Composable
fun SavingsLogo(
    tint: String,
    logoData: ByteArray?,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
) {
    val color = remember(tint) { colorFromHex(tint) }
    val bitmap = remember(logoData) {
        logoData?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        modifier = modifier.size(size).clip(shape).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size))
        } else {
            Icon(
                imageVector = sfSymbolIcon("lock.shield.fill"),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size * 0.45f),
            )
        }
    }
}

/** A savings pot in the list; also the live preview in the form (iOS `SavingsCard`). */
@Composable
fun SavingsCard(
    item: SavingsItem,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    onWithdraw: (() -> Unit)? = null,
) {
    val savings = item.savings
    val tint = remember(savings.tint) { colorFromHex(savings.tint) }

    Column(
        modifier = modifier.fillMaxWidth().finoraCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            SavingsLogo(tint = savings.tint, logoData = savings.logoData)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                AppText(savings.name, size = 16, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary, maxLines = 1)
                AppText(
                    text = item.account?.let { "in ${it.name}" } ?: "Account removed",
                    size = 12,
                    weight = AppFontWeight.Medium,
                    color = FinoraColors.TextSecondary,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                ShrinkToFitText(
                    text = CurrencyFormatter.taka(savings.currentSavings, showFraction = true),
                    style = appTextStyle(size = 17, weight = AppFontWeight.ExtraBold),
                    color = FinoraColors.TextPrimary,
                    minScale = 0.7f,
                    alignment = Alignment.CenterEnd,
                    modifier = Modifier.widthIn(max = 150.dp),
                )
                if (savings.isGoalReached) GoalReachedLabel(size = 11)
            }
        }

        val progress = savings.progress
        val target = savings.target
        if (progress != null && target != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProgressBar(fraction = progress, color = tint)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AppText(
                        "${(progress * 100).roundToInt()}% of ${CurrencyFormatter.taka(target)}",
                        size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                    )
                    savings.remainingToTarget?.takeIf { it > 0 }?.let {
                        AppText(
                            "${CurrencyFormatter.taka(it)} to go",
                            size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary,
                        )
                    }
                }
            }
        }

        if (onAdd != null && onWithdraw != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickButton(SavingsEntryKind.Deposit, enabled = true, onClick = onAdd, modifier = Modifier.weight(1f))
                QuickButton(
                    SavingsEntryKind.Withdrawal,
                    enabled = savings.currentSavings > 0,
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun GoalReachedLabel(size: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(sfSymbolIcon("checkmark.seal.fill"), contentDescription = null, tint = FinoraColors.Positive, modifier = Modifier.size((size + 2).dp))
        AppText("Goal reached", size = size, weight = AppFontWeight.Bold, color = FinoraColors.Positive, maxLines = 1)
    }
}

@Composable
private fun QuickButton(
    kind: SavingsEntryKind,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = kind.tint.color
    Row(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(sfSymbolIcon(kind.icon), contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        AppText(
            text = if (kind == SavingsEntryKind.Deposit) "Add" else "Withdraw",
            size = 13, weight = AppFontWeight.Bold, color = color,
        )
    }
}

/** One deposit / withdrawal line (iOS `SavingsEntryRow`). [savingsName] is shown above it in the combined log. */
@Composable
fun SavingsEntryRow(
    entry: SavingsEntry,
    modifier: Modifier = Modifier,
    savingsName: String? = null,
) {
    val color = entry.kind.tint.color
    val source = entry.account?.let { "${if (entry.kind == SavingsEntryKind.Deposit) "From" else "To"} ${it.name}" }
        ?: "Outside accounts"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sfSymbolIcon(entry.kind.icon), contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (savingsName != null) {
                AppText(savingsName, size = 11, weight = AppFontWeight.SemiBold, color = FinoraColors.TextTertiary, maxLines = 1)
            }
            AppText(entry.kind.historyTitle, size = 14, weight = AppFontWeight.Bold, color = FinoraColors.TextPrimary)
            AppText(
                "$source · ${entry.date.shortDisplay()}",
                size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextSecondary, maxLines = 1,
            )
            if (entry.note.isNotEmpty()) {
                AppText(entry.note, size = 12, weight = AppFontWeight.Medium, color = FinoraColors.TextTertiary, maxLines = 1)
            }
        }
        ShrinkToFitText(
            text = entry.kind.sign + CurrencyFormatter.taka(entry.amount, showFraction = true),
            style = appTextStyle(size = 15, weight = AppFontWeight.ExtraBold),
            color = if (entry.kind == SavingsEntryKind.Deposit) FinoraColors.Positive else FinoraColors.Negative,
            minScale = 0.7f,
            alignment = Alignment.CenterEnd,
            modifier = Modifier.widthIn(max = 140.dp),
        )
    }
}
