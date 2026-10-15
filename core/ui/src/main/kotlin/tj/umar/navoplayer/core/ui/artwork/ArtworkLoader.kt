package tj.umar.navoplayer.core.ui.artwork

import android.graphics.Bitmap
import androidx.compose.runtime.staticCompositionLocalOf
import tj.umar.navoplayer.core.domain.model.ArtworkKey

interface ArtworkLoader {
    fun cached(key: ArtworkKey, sizePx: Int): Bitmap?
    suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap?

    companion object {
        val None: ArtworkLoader = object : ArtworkLoader {
            override fun cached(key: ArtworkKey, sizePx: Int): Bitmap? = null
            override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap? = null
        }
    }
}

val LocalArtworkLoader = staticCompositionLocalOf { ArtworkLoader.None }
