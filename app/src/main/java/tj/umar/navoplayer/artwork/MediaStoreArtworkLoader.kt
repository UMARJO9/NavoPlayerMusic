package tj.umar.navoplayer.artwork

import android.graphics.Bitmap
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.mediastore.artwork.AudioArtworkSource
import tj.umar.navoplayer.core.ui.artwork.ArtworkLoader
import javax.inject.Inject

internal class MediaStoreArtworkLoader @Inject constructor(
    private val source: AudioArtworkSource,
    private val preference: CoverArtPreference,
) : ArtworkLoader {

    override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? =
        if (preference.isKnownEnabled) source.cached(key, sizePx) else null

    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? =
        if (preference.isEnabled()) source.load(key, sizePx) else null
}
