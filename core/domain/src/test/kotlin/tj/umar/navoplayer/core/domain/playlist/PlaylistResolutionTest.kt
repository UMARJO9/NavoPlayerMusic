package tj.umar.navoplayer.core.domain.playlist

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.Playlist
import tj.umar.navoplayer.core.domain.settings.TrackCatalog
import tj.umar.navoplayer.core.testing.data.TestTracks

class PlaylistResolutionTest {

    private val library = TrackCatalog(TestTracks.tracks.indexById())

    private fun playlist(vararg ids: Long) = Playlist(
        id = 7,
        name = "Mix",
        createdAtMs = 1,
        updatedAtMs = 2,
        trackIds = ids.toList(),
    )

    @Test
    fun `detail keeps stored order and skips missing tracks`() {
        val detail = playlist(TestTracks.longMix.id, 999, TestTracks.alpha.id).toDetail(library)

        assertEquals(listOf(TestTracks.longMix, TestTracks.alpha), detail.tracks)
        assertEquals(1, detail.missingTrackCount)
        assertEquals("Mix", detail.name)
    }

    @Test
    fun `summary counts only available tracks`() {
        val summary = playlist(TestTracks.alpha.id, TestTracks.beta.id, 999).toSummary(library)

        assertEquals(2, summary.trackCount)
        assertEquals(TestTracks.alpha.durationMs + TestTracks.beta.durationMs, summary.durationMs)
    }

    @Test
    fun `empty playlist resolves to empty detail`() {
        val detail = playlist().toDetail(library)

        assertEquals(emptyList<Any>(), detail.tracks)
        assertEquals(0, detail.missingTrackCount)
    }
}
