package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackProgressTest {

    @Test
    fun `zero duration gives zero fraction`() {
        assertEquals(0f, PlaybackProgress(positionMs = 1_000, durationMs = 0).fraction)
    }

    @Test
    fun `fraction is position over duration`() {
        assertEquals(0.25f, PlaybackProgress(positionMs = 1_000, durationMs = 4_000).fraction)
    }

    @Test
    fun `fraction is clamped to one`() {
        assertEquals(1f, PlaybackProgress(positionMs = 5_000, durationMs = 4_000).fraction)
    }
}
