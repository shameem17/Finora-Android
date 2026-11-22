package com.shameem.finora.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Semantic colours built from the three palette colours (iOS `Theme.swift`).
 * Screens use these names, never raw hex values.
 */
object FinoraColors {
    val BrandPrimary = CustomColor.Primary.color
    val BrandSecondary = CustomColor.Secondary.color
    val BrandAccent = CustomColor.Accent.color

    // Surfaces
    val ScreenBackground = BrandPrimary
    val CardSurface = Color.White.copy(alpha = 0.6f)
    val FillSubtle = BrandSecondary.copy(alpha = 0.07f)
    val Hairline = BrandSecondary.copy(alpha = 0.08f)

    // Text and icons
    val TextPrimary = BrandSecondary
    val TextSecondary = BrandSecondary.copy(alpha = 0.65f)
    val TextTertiary = BrandSecondary.copy(alpha = 0.42f)

    // Accent
    val Accent = BrandAccent
    val OnAccent = BrandSecondary

    // Hero cards
    val HeroSurface = BrandSecondary
    val OnHero = BrandPrimary
    val OnHeroSecondary = BrandPrimary.copy(alpha = 0.72f)

    // Money
    val Positive = CustomColor.MintGreen.color
    val Negative = CustomColor.Negative.color
}

/** Teal summary card surface with its soft shadow (iOS `.heroCard()`). */
fun Modifier.heroCard(cornerRadius: Dp = 28.dp): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = 14.dp,
            shape = shape,
            ambientColor = FinoraColors.BrandSecondary.copy(alpha = 0.25f),
            spotColor = FinoraColors.BrandSecondary.copy(alpha = 0.25f),
        )
        .background(FinoraColors.HeroSurface, shape)
}

private val FinoraColorScheme = lightColorScheme(
    primary = FinoraColors.BrandSecondary,
    onPrimary = FinoraColors.BrandPrimary,
    secondary = FinoraColors.BrandAccent,
    onSecondary = FinoraColors.BrandSecondary,
    background = FinoraColors.ScreenBackground,
    onBackground = FinoraColors.TextPrimary,
    surface = FinoraColors.ScreenBackground,
    onSurface = FinoraColors.TextPrimary,
    error = FinoraColors.Negative,
)

/** Single light appearance, as on iOS: the system dark-mode setting does not alter the palette. */
@Composable
fun FinoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FinoraColorScheme,
        typography = FinoraTypography,
        content = content,
    )
}
