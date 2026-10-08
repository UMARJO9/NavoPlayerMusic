package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.testing.data.TestTracks

class TrackDurationsTest {

    private fun trackOf(durationMs: Long) = TestTracks.alpha.copy(durationMs = durationMs)

    @Test
    fun `empty list has zero minutes`() {
        assertEquals(0, emptyList<Track>().totalDurationMinutes())
    }

    @Test
    fun `sums all tracks and rounds to nearest minute`() {
        assertEquals(66, TestTracks.tracks.totalDurationMinutes())
    }

    @Test
    fun `rounds half a minute up`() {
        assertEquals(0, listOf(trackOf(29_999)).totalDurationMinutes())
        assertEquals(1, listOf(trackOf(30_000)).totalDurationMinutes())
    }
}
