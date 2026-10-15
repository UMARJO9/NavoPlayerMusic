package tj.umar.navoplayer.core.mediastore.artwork

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.FileNotFoundException
import kotlin.coroutines.cancellation.CancellationException

internal class LegacyArtworkDecoder(
    private val context: Context,
) : ArtworkDecoder {

    override suspend fun album(albumId: Long, targetPx: Int): Bitmap? {
        currentCoroutineContext().ensureActive()
        val uri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                decodeSampled(targetPx) { options -> BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options) }
            }
        } catch (e: FileNotFoundException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override suspend fun track(trackId: Long, targetPx: Int): Bitmap? {
        currentCoroutineContext().ensureActive()
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
        val picture = embeddedPicture(uri) ?: return null
        currentCoroutineContext().ensureActive()
        return decodeSampled(targetPx) { options -> BitmapFactory.decodeByteArray(picture, 0, picture.size, options) }
    }

    private fun embeddedPicture(uri: Uri): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.embeddedPicture
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            throw e
        } catch (e: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }

    private companion object {
        val ALBUM_ART_URI: Uri = Uri.parse("content://media/external/audio/albumart")
    }
}

private inline fun decodeSampled(targetPx: Int, decode: (BitmapFactory.Options) -> Bitmap?): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    decode(bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, targetPx)
    }
    return decode(options)
}
