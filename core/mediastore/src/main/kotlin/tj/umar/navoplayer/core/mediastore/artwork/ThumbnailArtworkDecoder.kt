package tj.umar.navoplayer.core.mediastore.artwork

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import java.io.IOException

@RequiresApi(29)
internal class ThumbnailArtworkDecoder(
    private val context: Context,
) : ArtworkDecoder {

    override suspend fun album(albumId: Long, targetPx: Int): Bitmap? =
        thumbnail(ContentUris.withAppendedId(MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL), albumId), targetPx)

    override suspend fun track(trackId: Long, targetPx: Int): Bitmap? =
        thumbnail(ContentUris.withAppendedId(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL), trackId), targetPx)

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun thumbnail(uri: Uri, targetPx: Int): Bitmap? {
        val coroutineContext = currentCoroutineContext()
        coroutineContext.ensureActive()
        val cancellationSignal = CancellationSignal()
        val cancelHandle = coroutineContext.job.invokeOnCompletion(onCancelling = true) { cancellationSignal.cancel() }
        return try {
            context.contentResolver.loadThumbnail(uri, Size(targetPx, targetPx), cancellationSignal)
        } catch (e: OperationCanceledException) {
            coroutineContext.ensureActive()
            throw e
        } catch (e: IOException) {
            null
        } finally {
            cancelHandle.dispose()
        }
    }
}
