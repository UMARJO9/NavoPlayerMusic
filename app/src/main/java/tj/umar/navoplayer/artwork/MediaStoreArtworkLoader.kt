package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader
import javax.inject.Inject

internal class MediaStoreArtworkLoader @Inject constructor(
    private val source: AudioArtworkSource,
) : ArtworkLoader {

    override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? = source.cached(key, sizePx)

    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? = source.load(key, sizePx)
}
