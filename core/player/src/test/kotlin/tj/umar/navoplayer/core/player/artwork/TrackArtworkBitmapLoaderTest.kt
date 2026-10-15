package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import tj.umar.navoplayer.core.domain.model.ArtworkKey
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutionException

private class RecordingBitmapLoader(private val bitmap: Bitmap) : BitmapLoader {
    val loaded = mutableListOf<Uri>()
    override fun supportsMimeType(mimeType: String): Boolean = true
    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = Futures.immediateFuture(bitmap)
    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        loaded += uri
        return Futures.immediateFuture(bitmap)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TrackArtworkBitmapLoaderTest {

    private val fallbackBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val fallback = RecordingBitmapLoader(fallbackBitmap)
    private val renderedIds = mutableListOf<Long>()

    private fun png(side: Int): ByteArray = ByteArrayOutputStream().use { stream ->
        Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888).compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.toByteArray()
    }

    private fun loader(render: (Long) -> ByteArray?) = TrackArtworkBitmapLoader(
        artwork = { id -> renderedIds += id; render(id) },
        fallback = fallback,
        executor = MoreExecutors.newDirectExecutorService(),
    )

    @Test
    fun `artwork uri round trips track and album`() {
        assertEquals(ArtworkKey(42, 7), trackArtworkUri(ArtworkKey(42, 7)).artworkKey())
        assertEquals(ArtworkKey(42, null), trackArtworkUri(ArtworkKey(42, null)).artworkKey())
    }

    @Test
    fun `malformed album id is dropped`() {
        assertEquals(ArtworkKey(42, null), Uri.parse("navo-artwork://track/42?album=abc").artworkKey())
    }

    @Test
    fun `foreign or malformed uris have no key`() {
        assertNull(Uri.parse("content://media/external/audio/media/42").artworkKey())
        assertNull(Uri.parse("navo-artwork://track/abc").artworkKey())
        assertNull(Uri.parse("navo-artwork://album/42").artworkKey())
    }

    @Test
    fun `track artwork uri renders medallion`() {
        val bitmap = loader { png(8) }.loadBitmap(trackArtworkUri(ArtworkKey(7, null))).get()

        assertEquals(8, bitmap.width)
        assertEquals(listOf(7L), renderedIds)
        assertTrue(fallback.loaded.isEmpty())
    }

    @Test
    fun `other uris go to fallback`() {
        val uri = Uri.parse("content://media/external/images/1")

        val bitmap = loader { png(8) }.loadBitmap(uri).get()

        assertSame(fallbackBitmap, bitmap)
        assertEquals(listOf(uri), fallback.loaded)
        assertTrue(renderedIds.isEmpty())
    }

    @Test
    fun `missing artwork fails future`() {
        val failure = runCatching { loader { null }.loadBitmap(trackArtworkUri(ArtworkKey(3, null))).get() }.exceptionOrNull()

        assertTrue(failure is ExecutionException)
    }
}
