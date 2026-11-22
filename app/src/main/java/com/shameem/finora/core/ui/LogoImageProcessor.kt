package com.shameem.finora.core.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Shrinks a user-picked image into a small PNG suitable for an account/savings logo. */
object LogoImageProcessor {

    /**
     * Scales [data] so its longest side is at most [maxDimension] px (never upscales) and returns PNG
     * bytes, or null if [data] is not a decodable image. Runs off the main thread.
     */
    suspend fun makeLogo(data: ByteArray, maxDimension: Int = 160): ByteArray? = withContext(Dispatchers.Default) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
        val longestSide = max(bounds.outWidth, bounds.outHeight)
        if (longestSide <= 0) return@withContext null

        // Decode a power-of-two smaller bitmap first so huge photos never exhaust memory.
        var sample = 1
        while (longestSide / (sample * 2) >= maxDimension) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: return@withContext null

        val ratio = min(maxDimension.toFloat() / max(decoded.width, decoded.height), 1f)
        val width = max((decoded.width * ratio).roundToInt(), 1)
        val height = max((decoded.height * ratio).roundToInt(), 1)
        val scaled = if (width == decoded.width && height == decoded.height) {
            decoded
        } else {
            Bitmap.createScaledBitmap(decoded, width, height, true)
        }

        ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.toByteArray()
        }
    }
}
