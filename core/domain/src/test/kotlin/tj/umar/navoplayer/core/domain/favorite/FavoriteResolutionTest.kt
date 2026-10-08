package tj.umar.navoplayer.core.domain.favorite

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.FavoritesSummary
import tj.umar.navoplayer.core.domain.playlist.indexById
import tj.umar.navoplayer.core.domain.settings.TrackCatalog
import tj.umar.navoplayer.core.testing.data.TestTracks

class FavoriteResolutionTest {

    private val library = TrackCatalog(TestTracks.tracks.indexById())

    @Test
    fun `tracks keep order and missing ones are counted`() {
        val favorites = listOf(TestTracks.longMix.id, 999, TestTracks.alpha.id).toFavoriteTracks(library)

        assertEquals(listOf(TestTracks.longMix, TestTracks.alpha), favorites.tracks)
        assertEquals(1, favorites.missingTrackCount)
    }

    @Test
    fun `summary counts available tracks and clamps negative durations`() {
        val broken = TestTracks.beta.copy(id = 50, durationMs = -5)
        val summary = listOf(TestTracks.alpha.id, broken.id, 999).toFavoritesSummary(TrackCatalog(library.tracksById + (broken.id to broken)))

        assertEquals(FavoritesSummary(trackCount = 2, durationMs = TestTracks.alpha.durationMs), summary)
    }

    @Test
    fun `no favorites gives empty summary`() {
        assertEquals(FavoritesSummary.Empty, emptyList<Long>().toFavoritesSummary(library))
    }
}
