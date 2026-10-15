package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey

interface PlaybackArtwork {
    suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap
}
