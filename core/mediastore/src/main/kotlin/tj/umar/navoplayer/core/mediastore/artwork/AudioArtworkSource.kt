package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey

interface AudioArtworkSource {
    fun cached(key: ArtworkKey, sizePx: Int): Bitmap?
    suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap?
}
