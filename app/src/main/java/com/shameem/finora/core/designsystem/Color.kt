package com.shameem.finora.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * Parses `#RGB`, `#RRGGBB` or `#RRGGBBAA` (CSS channel order, same as the iOS `Color(hex:)`).
 * The leading `#` is optional. Invalid input yields [Color.Transparent].
 */
fun colorFromHex(hex: String?): Color {
    val cleaned = (hex ?: "000000").trim().removePrefix("#")
    val rgb = cleaned.toLongOrNull(16) ?: return Color.Transparent

    fun channel(shift: Int, mask: Long, max: Float): Float = ((rgb shr shift) and mask).toInt() / max

    return when (cleaned.length) {
        3 -> Color(red = channel(8, 0xF, 15f), green = channel(4, 0xF, 15f), blue = channel(0, 0xF, 15f))
        6 -> Color(red = channel(16, 0xFF, 255f), green = channel(8, 0xFF, 255f), blue = channel(0, 0xFF, 255f))
        8 -> Color(
            red = channel(24, 0xFF, 255f),
            green = channel(16, 0xFF, 255f),
            blue = channel(8, 0xFF, 255f),
            alpha = channel(0, 0xFF, 255f),
        )
        else -> Color.Transparent
    }
}

/** `#RRGGBB` for persisting a user-picked colour (iOS `Color.hexString`). */
fun Color.toHexString(): String {
    fun byte(v: Float) = (v.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    return "#%02X%02X%02X".format(byte(red), byte(green), byte(blue))
}

/**
 * The whole app is drawn with three colours in a 60 / 30 / 10 split (iOS `CustomColor`):
 * primary cream (backgrounds), secondary deep teal (text, hero cards, tab bar), accent tangerine.
 */
enum class CustomColor(val hex: String) {
    Primary("#F3EBDD"),
    Secondary("#013230"),
    Accent("#F29A3A"),
    Warning("#F5B52E"),
    Pest("#7A9D54"),
    Navy("#1E3A8A"),
    MintGreen("#10B981"),
    Violet("#610C63"),
    Negative("#E5383B");

    val color: Color = colorFromHex(hex)
}

/** Hex strings for places that store a colour as text (user-themed items). */
object Palette {
    val secondaryHex: String = CustomColor.Secondary.hex
}
