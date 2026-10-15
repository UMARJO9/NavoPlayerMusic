package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

internal const val ARTWORK_JPEG_QUALITY = 90

internal fun Bitmap.encodeJpeg(quality: Int = ARTWORK_JPEG_QUALITY): ByteArray =
    ByteArrayOutputStream().use { stream ->
        check(compress(Bitmap.CompressFormat.JPEG, quality, stream)) { "Artwork could not be encoded" }
        stream.toByteArray()
    }
