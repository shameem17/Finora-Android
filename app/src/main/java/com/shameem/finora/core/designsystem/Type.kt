package com.shameem.finora.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.shameem.finora.R

/** Manrope, bundled as static weights in `res/font` (same files as the iOS app). */
val Manrope = FontFamily(
    Font(R.font.manrope_extra_light, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semi_bold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extra_bold, FontWeight.ExtraBold),
)

/** Mirrors the iOS `AppFontWeight` enum. */
enum class AppFontWeight(val fontWeight: FontWeight) {
    ExtraLight(FontWeight.ExtraLight),
    Light(FontWeight.Light),
    Regular(FontWeight.Normal),
    Medium(FontWeight.Medium),
    SemiBold(FontWeight.SemiBold),
    Bold(FontWeight.Bold),
    ExtraBold(FontWeight.ExtraBold),
}

/** Equivalent of the iOS `.font(.manrope, size:, weight:)` modifier. */
fun appTextStyle(
    size: Int,
    weight: AppFontWeight = AppFontWeight.Regular,
    letterSpacing: TextUnit = TextUnit.Unspecified,
): TextStyle = TextStyle(
    fontFamily = Manrope,
    fontWeight = weight.fontWeight,
    fontSize = size.sp,
    letterSpacing = letterSpacing,
)

/** Material typography re-skinned with Manrope so framework-provided text matches the app. */
val FinoraTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = Manrope),
        displayMedium = base.displayMedium.copy(fontFamily = Manrope),
        displaySmall = base.displaySmall.copy(fontFamily = Manrope),
        headlineLarge = base.headlineLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold),
        titleMedium = base.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = Manrope),
        bodyMedium = base.bodyMedium.copy(fontFamily = Manrope),
        bodySmall = base.bodySmall.copy(fontFamily = Manrope),
        labelLarge = base.labelLarge.copy(fontFamily = Manrope),
        labelMedium = base.labelMedium.copy(fontFamily = Manrope),
        labelSmall = base.labelSmall.copy(fontFamily = Manrope),
    )
}
