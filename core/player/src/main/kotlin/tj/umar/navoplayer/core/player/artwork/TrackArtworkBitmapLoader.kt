package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.guava.future

internal const val SESSION_ARTWORK_PX = 512

@OptIn(UnstableApi::class)
internal class TrackArtworkBitmapLoader(
    private val artwork: PlaybackArtwork,
    private val fallback: BitmapLoader,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : BitmapLoader {

    override fun supportsMimeType(mimeType: String): Boolean = fallback.supportsMimeType(mimeType)

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = fallback.decodeBitmap(data)

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val key = uri.artworkKey() ?: return fallback.loadBitmap(uri)
        return scope.future(dispatcher) { artwork.load(key, SESSION_ARTWORK_PX).cappedToShortSide(SESSION_ARTWORK_PX) }
    }
}
