package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.designsystem.medallion.MAX_MEDALLION_BITMAP_PX
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.renderBitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.player.artwork.PlaybackArtwork
import javax.inject.Inject
import kotlin.math.roundToInt

internal class CoverOrMedallionPlaybackArtwork @Inject constructor(
    private val source: AudioArtworkSource,
    private val preference: CoverArtPreference,
) : PlaybackArtwork {

    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap =
        cover(key, sizePx)
            ?: MedallionPalettes.forKey(key.trackId).renderBitmap(minOf(sizePx, MAX_MEDALLION_BITMAP_PX), opaque = true)

    private suspend fun cover(key: ArtworkKey, sizePx: Int): Bitmap? =
        if (preference.isEnabled()) source.load(key, sizePx)?.scaledDownTo(sizePx) else null
}

private fun Bitmap.scaledDownTo(sizePx: Int): Bitmap {
    val shortSide = minOf(width, height)
    if (sizePx <= 0 || shortSide <= sizePx) return this
    val scale = sizePx.toFloat() / shortSide
    return Bitmap.createScaledBitmap(
        this,
        (width * scale).roundToInt().coerceAtLeast(1),
        (height * scale).roundToInt().coerceAtLeast(1),
        true,
    )
}
