package tj.umar.navoplayer.core.mediastore.artwork

import android.graphics.Bitmap
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ArtworkMemoryCacheTest {

    private val cache = ArtworkMemoryCache(maxBytes = 8 * 1024 * 1024)
    private val album = ArtworkCacheId.Album(1)

    @Test
    fun `returns bitmap stored at requested bucket`() {
        val bitmap = bitmap()
        cache.put(album, 256, bitmap)

        assertSame(bitmap, cache.getAtLeast(album, 256))
    }

    @Test
    fun `serves smaller request from larger bucket`() {
        val bitmap = bitmap()
        cache.put(album, 512, bitmap)

        assertSame(bitmap, cache.getAtLeast(album, 128))
    }

    @Test
    fun `does not serve larger request from smaller bucket`() {
        cache.put(album, 128, bitmap())

        assertNull(cache.getAtLeast(album, 256))
    }

    @Test
    fun `album and track ids are separate`() {
        cache.put(album, 256, bitmap())

        assertNull(cache.getAtLeast(ArtworkCacheId.Track(1), 256))
    }

    @Test
    fun `misses are remembered until cleared`() {
        cache.putMiss(album)
        assertTrue(cache.isMiss(album))

        cache.clearMisses()

        assertFalse(cache.isMiss(album))
    }

    @Test
    fun `storing a bitmap clears its miss`() {
        cache.putMiss(album)

        cache.put(album, 256, bitmap())

        assertFalse(cache.isMiss(album))
    }

    @Test
    fun `evicts least recently used bitmaps beyond budget`() {
        val small = ArtworkMemoryCache(maxBytes = 64 * 64 * 4)
        small.put(ArtworkCacheId.Album(1), 128, bitmap())
        small.put(ArtworkCacheId.Album(2), 128, bitmap())

        assertNull(small.getAtLeast(ArtworkCacheId.Album(1), 128))
    }

    private fun bitmap(): Bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
}
