package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import kotlin.math.roundToInt

internal fun Bitmap.cappedToShortSide(maxPx: Int): Bitmap {
    val shortSide = minOf(width, height)
    if (maxPx <= 0 || shortSide <= maxPx) return this
    val scale = maxPx.toFloat() / shortSide
    return Bitmap.createScaledBitmap(
        this,
        (width * scale).roundToInt().coerceAtLeast(1),
        (height * scale).roundToInt().coerceAtLeast(1),
        true,
    )
}
