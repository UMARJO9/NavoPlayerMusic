package tj.umar.navoplayer.core.designsystem.medallion

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.io.ByteArrayOutputStream

const val MAX_MEDALLION_BITMAP_PX = 256

fun MedallionPalette.renderBitmap(
    sizePx: Int,
    variant: MedallionVariant = MedallionVariant.Simple,
): Bitmap {
    val side = sizePx.coerceIn(1, MAX_MEDALLION_BITMAP_PX)
    val image = ImageBitmap(side, side)
    val size = Size(side.toFloat(), side.toFloat())
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(image), size) {
        when (variant) {
            MedallionVariant.Simple -> drawSimpleMedallion(this@renderBitmap)
            MedallionVariant.Detailed -> drawDetailedMedallion(this@renderBitmap)
        }
    }
    return image.asAndroidBitmap()
}

fun MedallionPalette.renderPng(sizePx: Int = MAX_MEDALLION_BITMAP_PX): ByteArray {
    val bitmap = renderBitmap(sizePx)
    return try {
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
            stream.toByteArray()
        }
    } finally {
        bitmap.recycle()
    }
}

private const val PNG_QUALITY = 100
