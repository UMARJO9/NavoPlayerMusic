package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService

@OptIn(UnstableApi::class)
internal class TrackArtworkBitmapLoader(
    private val artwork: ResumptionArtwork,
    private val fallback: BitmapLoader,
    private val executor: ListeningExecutorService,
) : BitmapLoader {

    override fun supportsMimeType(mimeType: String): Boolean = fallback.supportsMimeType(mimeType)

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = fallback.decodeBitmap(data)

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val trackId = uri.artworkTrackId() ?: return fallback.loadBitmap(uri)
        return executor.submit<Bitmap> {
            val png = checkNotNull(artwork.render(trackId)) { "No artwork for track $trackId" }
            checkNotNull(BitmapFactory.decodeByteArray(png, 0, png.size)) { "Undecodable artwork for track $trackId" }
        }
    }
}
