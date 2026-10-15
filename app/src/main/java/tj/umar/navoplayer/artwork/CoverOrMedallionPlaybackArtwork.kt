package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.designsystem.medallion.MAX_MEDALLION_BITMAP_PX
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.medallion.renderBitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.player.artwork.PlaybackArtwork
import javax.inject.Inject

internal class CoverOrMedallionPlaybackArtwork @Inject constructor(
    private val source: AudioArtworkSource,
) : PlaybackArtwork {

    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap =
        source.load(key, sizePx)
            ?: MedallionPalettes.forKey(key.trackId).renderBitmap(minOf(sizePx, MAX_MEDALLION_BITMAP_PX), opaque = true)
}
