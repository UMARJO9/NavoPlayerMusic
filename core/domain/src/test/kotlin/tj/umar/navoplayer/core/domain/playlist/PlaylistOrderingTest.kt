package tj.umar.navoplayer.core.domain.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.testing.data.TestTracks

class PlaylistOrderingTest {

    private val alpha = TestTracks.alpha
    private val beta = TestTracks.beta
    private val longMix = TestTracks.longMix
    private val alphaTwo = TestTracks.alphaTwo
    private val tracks = listOf(alpha, beta, longMix, alphaTwo)

    @Test
    fun `movedTo moves id down`() {
        assertEquals(listOf(2L, 3L, 1L, 4L), listOf(1L, 2L, 3L, 4L).movedTo(1L, 2))
    }

    @Test
    fun `movedTo moves id up`() {
        assertEquals(listOf(4L, 1L, 2L, 3L), listOf(1L, 2L, 3L, 4L).movedTo(4L, 0))
    }

    @Test
    fun `movedTo clamps target index`() {
        assertEquals(listOf(2L, 3L, 1L), listOf(1L, 2L, 3L).movedTo(1L, 10))
        assertEquals(listOf(3L, 1L, 2L), listOf(1L, 2L, 3L).movedTo(3L, -5))
    }

    @Test
    fun `movedTo keeps list for unknown id`() {
        val ids = listOf(1L, 2L, 3L)

        assertSame(ids, ids.movedTo(9L, 0))
    }

    @Test
    fun `projected without order drops hidden tracks only`() {
        assertEquals(listOf(alpha, longMix, alphaTwo), tracks.projected(order = null, hidden = setOf(beta.id)))
    }

    @Test
    fun `projected sorts by order`() {
        val order = listOf(alphaTwo.id, alpha.id, longMix.id, beta.id)

        assertEquals(listOf(alphaTwo, alpha, longMix, beta), tracks.projected(order, hidden = emptySet()))
    }

    @Test
    fun `projected keeps tracks missing from order at end in stored order`() {
        val order = listOf(longMix.id, alpha.id)

        assertEquals(listOf(longMix, alpha, beta, alphaTwo), tracks.projected(order, hidden = emptySet()))
    }

    @Test
    fun `projected ignores stale order ids and hidden tracks`() {
        val order = listOf(999L, alphaTwo.id, beta.id, alpha.id)

        assertEquals(listOf(alphaTwo, alpha, longMix), tracks.projected(order, hidden = setOf(beta.id)))
    }

    @Test
    fun `matchesOrder compares relative order of shared ids`() {
        assertTrue(tracks.matchesOrder(tracks.map { it.id }))
        assertTrue(tracks.matchesOrder(listOf(alpha.id, longMix.id, 999L)))
        assertFalse(tracks.matchesOrder(listOf(beta.id, alpha.id, longMix.id, alphaTwo.id)))
    }
}
