package com.shameem.finora.screens.home.view

import android.graphics.BitmapFactory
import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shameem.finora.core.designsystem.AppFontWeight
import com.shameem.finora.core.designsystem.appTextStyle
import com.shameem.finora.core.designsystem.colorFromHex
import com.shameem.finora.core.ui.AppText
import com.shameem.finora.core.ui.ShrinkToFitText
import com.shameem.finora.core.ui.sfSymbolIcon
import com.shameem.finora.core.util.CurrencyFormatter
import com.shameem.finora.screens.home.model.Account
import kotlin.math.max
import kotlin.math.min

private val CardShape = RoundedCornerShape(26.dp)

/** Slightly more saturated, darker shade; the gradient end colour (iOS `Color.darker`). */
fun Color.darker(amount: Float = 0.25f): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsv)
    hsv[1] = min(hsv[1] + 0.05f, 1f)
    hsv[2] = max(hsv[2] - amount, 0f)
    return Color(android.graphics.Color.HSVToColor((alpha * 255f).toInt(), hsv))
}

/** `1234567894821` -> `•••• 4821`; no digits -> "No account number". */
fun maskedNumber(number: String): String {
    val last4 = number.filter(Char::isDigit).takeLast(4)
    return if (last4.isEmpty()) "No account number" else "•••• $last4"
}

/** Gradient tinted card face with two soft decorative circles (iOS `CardBackground`). */
@Composable
fun CardBackground(tint: String, modifier: Modifier = Modifier) {
    val base = remember(tint) { colorFromHex(tint) }
    val gradient = remember(base) { Brush.linearGradient(listOf(base, base.darker(0.3f))) }
    Canvas(modifier.background(gradient)) {
        drawCircle(
            color = Color.White.copy(alpha = 0.12f),
            radius = 75.dp.toPx(),
            center = Offset(size.width / 2f + 70.dp.toPx(), size.height / 2f - 60.dp.toPx()),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.07f),
            radius = 55.dp.toPx(),
            center = Offset(size.width / 2f - 80.dp.toPx(), size.height / 2f + 80.dp.toPx()),
        )
    }
}

/** A wallet-style card for one account: provider logo, type chip, masked number and balance. */
@Composable
fun AccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    showsContent: Boolean = true,
) {
    val border = remember {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.35f), Color.White.copy(alpha = 0.02f)))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 176.dp)
            .clip(CardShape)
            .border(1.dp, border, CardShape),
    ) {
        CardBackground(tint = account.tint, modifier = Modifier.matchParentSize())
        AccountCardContent(account, Modifier.alpha(if (showsContent) 1f else 0f))
    }
}

@Composable
private fun BoxScope.AccountCardContent(account: Account, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 176.dp)
            .padding(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            ProviderLogo(account)
            Spacer(Modifier.weight(1f))
            TypeChip(account.type)
        }

        Spacer(Modifier.weight(1f).heightIn(min = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            ShrinkToFitText(
                text = account.name,
                style = appTextStyle(size = 17, weight = AppFontWeight.SemiBold),
                color = Color.White,
                minScale = 0.7f,
                modifier = Modifier.fillMaxWidth(),
            )
            androidx.compose.material3.Text(
                text = maskedNumber(account.number),
                color = Color.White.copy(alpha = 0.7f),
                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            )
        }

        Spacer(Modifier.weight(1f).heightIn(min = 14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            AppText(
                text = "BALANCE",
                size = 11,
                weight = AppFontWeight.Medium,
                color = Color.White.copy(alpha = 0.65f),
                letterSpacing = 0.8.sp,
            )
            ShrinkToFitText(
                text = CurrencyFormatter.taka(account.balance, showFraction = true),
                style = appTextStyle(size = 24, weight = AppFontWeight.Bold),
                color = Color.White,
                minScale = 0.6f,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ProviderLogo(account: Account) {
    val shape = RoundedCornerShape(12.dp)
    val bitmap = remember(account.logoData) {
        account.logoData?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
    }
    val logoRes = rememberBundledLogoRes(account.logo)

    Box(
        modifier = Modifier
            .size(42.dp)
            .shadow(4.dp, shape, ambientColor = Color.Black.copy(alpha = 0.15f), spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(shape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        when {
            bitmap != null -> Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp),
            )
            logoRes != 0 -> Image(
                painter = painterResource(logoRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(7.dp),
            )
            else -> AppText(
                text = account.name.take(2).uppercase(),
                size = 15,
                weight = AppFontWeight.ExtraBold,
                color = colorFromHex(account.tint),
            )
        }
    }
}

/** Optional bundled provider logo (`res/drawable/<name>`); 0 when none is bundled (iOS `UIImage(named:)`). */
@SuppressLint("DiscouragedApi")
@Composable
private fun rememberBundledLogoRes(name: String?): Int {
    val context = LocalContext.current
    return remember(name) {
        if (name.isNullOrBlank()) 0 else context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
private fun TypeChip(type: AccountType) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(sfSymbolIcon(type.symbol), contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
        AppText(text = type.title, size = 11, weight = AppFontWeight.SemiBold, color = Color.White, maxLines = 1)
    }
}
