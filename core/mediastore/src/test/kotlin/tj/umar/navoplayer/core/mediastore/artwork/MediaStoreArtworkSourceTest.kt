package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import android.os.OperationCanceledException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
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
import tj.umar.navoplayer.core.domain.model.ArtworkKey

@RunWith(RobolectricTestRunner::class)
class MediaStoreArtworkSourceTest {

    private val decoder = FakeArtworkDecoder()
    private val mediaSource = FakeAudioMediaSource()
    private val albumTrack = ArtworkKey(trackId = 1, albumId = 10)
    private val looseTrack = ArtworkKey(trackId = 1, albumId = null)

    @Test
    fun `cached bitmap skips the decoder`() = runTest {
        val source = source()
        val art = bitmap()
        decoder.albumBitmaps[10] = art

        source.load(albumTrack, 200)
        source.load(albumTrack, 200)

        assertSame(art, source.cached(albumTrack, 200))
        assertEquals(listOf("album:10"), decoder.calls)
    }

    @Test
    fun `album art is tried before track art`() = runTest {
        val source = source()
        val art = bitmap()
        decoder.trackBitmaps[1] = art

        val result = source.load(albumTrack, 200)

        assertSame(art, result)
        assertEquals(listOf("album:10", "track:1"), decoder.calls)
    }

    @Test
    fun `track without album uses track art only`() = runTest {
        val source = source()

        source.load(looseTrack, 200)

        assertEquals(listOf("track:1"), decoder.calls)
    }

    @Test
    fun `missing art is negative cached`() = runTest {
        val source = source()

        assertNull(source.load(looseTrack, 200))
        assertNull(source.load(looseTrack, 200))

        assertEquals(listOf("track:1"), decoder.calls)
    }

    @Test
    fun `security failure is not cached`() = runTest {
        val source = source()
        decoder.failure = SecurityException("denied")

        assertNull(source.load(looseTrack, 200))
        decoder.failure = null
        val art = bitmap()
        decoder.trackBitmaps[1] = art

        assertSame(art, source.load(looseTrack, 200))
        assertEquals(listOf("track:1", "track:1"), decoder.calls)
    }

    @Test
    fun `decoded art is capped at the requested bucket`() = runTest {
        val source = source()
        decoder.albumBitmaps[10] = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)

        val result = source.load(albumTrack, 200)

        assertEquals(256, result?.width)
        assertSame(result, source.cached(albumTrack, 200))
    }

    @Test
    fun `unexpected decode failures are not cached`() = runTest {
        val source = source()
        val failures = listOf(
            OperationCanceledException(),
            IllegalArgumentException("bad uri"),
            UnsupportedOperationException("no provider"),
        )

        failures.forEach { failure ->
            decoder.failure = failure
            assertNull(source.load(looseTrack, 200))
        }
        decoder.failure = null
        val art = bitmap()
        decoder.trackBitmaps[1] = art

        assertSame(art, source.load(looseTrack, 200))
        assertEquals(4, decoder.calls.size)
    }

    @Test
    fun `decoder cancellation is rethrown`() = runTest {
        val source = source()
        decoder.failure = CancellationException("stopped")

        val failure = runCatching { source.load(looseTrack, 200) }.exceptionOrNull()

        assertTrue(failure is CancellationException)
    }

    @Test
    fun `tracks of the same album share one decode`() = runTest {
        val source = source()
        val gate = CompletableDeferred<Unit>()
        decoder.gate = gate
        decoder.albumBitmaps[10] = bitmap()

        val first = async { source.load(ArtworkKey(trackId = 1, albumId = 10), 200) }
        val second = async { source.load(ArtworkKey(trackId = 2, albumId = 10), 200) }
        runCurrent()
        gate.complete(Unit)

        assertSame(first.await(), second.await())
        assertEquals(listOf("album:10"), decoder.calls)
    }

    @Test
    fun `library change clears negative cache`() = runTest {
        val source = source()
        assertNull(source.load(looseTrack, 200))
        runCurrent()

        mediaSource.changes.emit(Unit)
        runCurrent()
        source.load(looseTrack, 200)

        assertEquals(listOf("track:1", "track:1"), decoder.calls)
    }

    @Test
    fun `cancelling the caller cancels the load`() = runTest {
        val source = source()
        decoder.gate = CompletableDeferred()

        val load = async { source.load(looseTrack, 200) }
        runCurrent()
        load.cancel()
        runCurrent()

        assertTrue(load.isCancelled)
        decoder.gate = null
        val art = bitmap()
        decoder.trackBitmaps[1] = art
        assertSame(art, source.load(looseTrack, 200))
        assertEquals(listOf("track:1", "track:1"), decoder.calls)
    }

    private fun TestScope.source() = MediaStoreArtworkSource(
        decoder = decoder,
        audioMediaSource = mediaSource,
        ioDispatcher = StandardTestDispatcher(testScheduler),
        appScope = backgroundScope,
    )

    private fun bitmap(): Bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
}
