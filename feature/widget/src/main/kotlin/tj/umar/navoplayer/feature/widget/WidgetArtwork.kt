package tj.umar.navoplayer.feature.widget

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.renderBitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader

internal suspend fun widgetArtwork(loader: ArtworkLoader, key: ArtworkKey, sizePx: Int): Bitmap =
    loader.load(key, sizePx)?.circleCropped(sizePx) ?: MedallionPalettes.forKey(key.trackId).renderBitmap(sizePx)

internal fun ArtworkLoader.enabledWhen(enabled: Boolean): ArtworkLoader = if (enabled) this else ArtworkLoader.None

internal fun Bitmap.circleCropped(sidePx: Int): Bitmap {
    val side = sidePx.coerceAtLeast(1)
    val output = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
    val scale = side.toFloat() / minOf(width, height).coerceAtLeast(1)
    val matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate((side - width * scale) / 2f, (side - height * scale) / 2f)
    }
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        shader = BitmapShader(this@circleCropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP).apply {
            setLocalMatrix(matrix)
        }
    }
    val radius = side / 2f
    Canvas(output).drawCircle(radius, radius, radius, paint)
    return output
}
