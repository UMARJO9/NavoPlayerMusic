package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.FavoritesSummary
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeFavoritesRepository
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class FavoriteUseCasesTest {

    private val tracks = FakeTrackRepository()
    private val favorites = FakeFavoritesRepository(listOf(TestTracks.longMix.id, 999, TestTracks.alpha.id))

    @Test
    fun `favorite tracks resolve against library and follow changes`() = runTest {
        val observe = ObserveFavoriteTracksUseCase(favorites, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(TestTracks.tracks)
            val first = awaitItem()
            assertEquals(listOf(TestTracks.longMix, TestTracks.alpha), first.tracks)
            assertEquals(1, first.missingTrackCount)

            favorites.addFavorite(TestTracks.beta.id)
            assertEquals(TestTracks.beta, awaitItem().tracks.first())

            tracks.emit(listOf(TestTracks.beta))
            assertEquals(listOf(TestTracks.beta), awaitItem().tracks)
        }
    }

    @Test
    fun `favorite tracks pass errors`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val observe = ObserveFavoriteTracksUseCase(favorites, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe().test {
            assertEquals("scan failed", awaitError().message)
        }
    }

    @Test
    fun `is favorite follows writes`() = runTest {
        ObserveIsFavoriteUseCase(favorites)(TestTracks.beta.id).test {
            assertFalse(awaitItem())
            favorites.addFavorite(TestTracks.beta.id)
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `set favorite adds and removes`() = runTest {
        val setFavorite = SetFavoriteUseCase(favorites)

        assertEquals(NavoResult.Success(true), setFavorite(TestTracks.beta.id, true))
        assertEquals(NavoResult.Success(false), setFavorite(TestTracks.beta.id, true))
        assertEquals(NavoResult.Success(true), setFavorite(TestTracks.beta.id, false))
        assertFalse(TestTracks.beta.id in favorites.current)
    }

    @Test
    fun `set favorite reports failure`() = runTest {
        favorites.writeError = IllegalStateException("locked")

        assertTrue(SetFavoriteUseCase(favorites)(1, true) is NavoResult.Error)
    }

    @Test
    fun `overview combines favorites and playlists with one scan`() = runTest {
        val playlists = FakePlaylistRepository(TestPlaylists.all)
        val observe = ObservePlaylistsOverviewUseCase(
            playlists,
            favorites,
            ObserveTracksUseCase(tracks, FakeSettingsRepository()),
            UnconfinedTestDispatcher(testScheduler),
        )

        observe().test {
            tracks.emit(TestTracks.tracks)
            val overview = awaitItem()
            assertEquals(
                FavoritesSummary(trackCount = 2, durationMs = TestTracks.longMix.durationMs + TestTracks.alpha.durationMs),
                overview.favorites,
            )
            assertEquals(listOf(TestPlaylists.empty.id, TestPlaylists.morning.id), overview.playlists.map { it.id })
        }
        assertEquals(1, tracks.observeCalls)
    }

    @Test
    fun `overview without favorites has empty summary`() = runTest {
        val observe = ObservePlaylistsOverviewUseCase(
            FakePlaylistRepository(),
            FakeFavoritesRepository(),
            ObserveTracksUseCase(tracks, FakeSettingsRepository()),
            UnconfinedTestDispatcher(testScheduler),
        )

        observe().test {
            tracks.emit(TestTracks.tracks)
            assertEquals(FavoritesSummary.Empty, awaitItem().favorites)
        }
    }
}
