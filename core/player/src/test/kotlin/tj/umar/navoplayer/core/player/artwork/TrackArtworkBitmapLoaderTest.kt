package tj.umar.navoplayer.core.player.artwork

import android.graphics.Bitmap
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.ArtworkKey

private class RecordingBitmapLoader(private val bitmap: Bitmap) : BitmapLoader {
    val loaded = mutableListOf<Uri>()
    override fun supportsMimeType(mimeType: String): Boolean = true
    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = Futures.immediateFuture(bitmap)
    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        loaded += uri
        return Futures.immediateFuture(bitmap)
    }
}

private class RecordingPlaybackArtwork(private val load: suspend (ArtworkKey) -> Bitmap) : PlaybackArtwork {
    val requests = mutableListOf<Pair<ArtworkKey, Int>>()
    override suspend fun load(key: ArtworkKey, sizePx: Int): Bitmap {
        requests += key to sizePx
        return load(key)
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrackArtworkBitmapLoaderTest {

    private val fallbackBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private val fallback = RecordingBitmapLoader(fallbackBitmap)

    private fun TestScope.loader(artwork: PlaybackArtwork) = TrackArtworkBitmapLoader(
        artwork = artwork,
        fallback = fallback,
        scope = CoroutineScope(SupervisorJob(backgroundScope.coroutineContext[Job])),
        dispatcher = StandardTestDispatcher(testScheduler),
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
    fun `track artwork uri loads playback artwork`() = runTest {
        val cover = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val artwork = RecordingPlaybackArtwork { cover }

        val bitmap = loader(artwork).loadBitmap(trackArtworkUri(ArtworkKey(7, 70))).await()

        assertSame(cover, bitmap)
        assertEquals(listOf(ArtworkKey(7, 70) to SESSION_ARTWORK_PX), artwork.requests)
        assertTrue(fallback.loaded.isEmpty())
    }

    @Test
    fun `other uris go to fallback`() = runTest {
        val uri = Uri.parse("content://media/external/images/1")
        val artwork = RecordingPlaybackArtwork { error("unused") }

        val bitmap = loader(artwork).loadBitmap(uri).await()

        assertSame(fallbackBitmap, bitmap)
        assertEquals(listOf(uri), fallback.loaded)
        assertTrue(artwork.requests.isEmpty())
    }

    @Test
    fun `artwork failure fails future`() = runTest {
        val artwork = RecordingPlaybackArtwork { error("broken") }

        val failure = runCatching { loader(artwork).loadBitmap(trackArtworkUri(ArtworkKey(3, null))).await() }.exceptionOrNull()

        assertTrue(failure is IllegalStateException)
    }

    @Test
    fun `cancelling future cancels the load`() = runTest {
        var cancelled = false
        val artwork = RecordingPlaybackArtwork {
            try {
                CompletableDeferred<Bitmap>().await()
            } finally {
                cancelled = true
            }
        }

        val future = loader(artwork).loadBitmap(trackArtworkUri(ArtworkKey(3, null)))
        runCurrent()
        future.cancel(true)
        runCurrent()

        assertTrue(cancelled)
    }
}
