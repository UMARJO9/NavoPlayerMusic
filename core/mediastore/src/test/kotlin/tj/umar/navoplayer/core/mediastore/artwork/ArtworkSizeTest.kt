package tj.umar.navoplayer.core.mediastore.artwork

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.ArtworkKey

class ArtworkSizeTest {

    @Test
    fun `small requests use the smallest bucket`() {
        assertEquals(128, artworkBucketPx(1))
        assertEquals(128, artworkBucketPx(128))
    }

    @Test
    fun `requests round up to the next bucket`() {
        assertEquals(256, artworkBucketPx(129))
        assertEquals(512, artworkBucketPx(300))
        assertEquals(1024, artworkBucketPx(513))
    }

    @Test
    fun `large requests are capped`() {
        assertEquals(1024, artworkBucketPx(4096))
    }

    @Test
    fun `sample size keeps short side at least target`() {
        assertEquals(1, sampleSizeFor(width = 300, height = 300, targetPx = 256))
        assertEquals(2, sampleSizeFor(width = 512, height = 600, targetPx = 256))
        assertEquals(4, sampleSizeFor(width = 3000, height = 2048, targetPx = 512))
    }

    @Test
    fun `sample size is one for degenerate input`() {
        assertEquals(1, sampleSizeFor(width = 0, height = 100, targetPx = 64))
        assertEquals(1, sampleSizeFor(width = 100, height = 100, targetPx = 0))
    }

    @Test
    fun `album key maps to album cache id`() {
        assertEquals(ArtworkCacheId.Album(5), ArtworkKey(1, 5).cacheId())
        assertEquals(ArtworkCacheId.Track(1), ArtworkKey(1, null).cacheId())
    }
}
