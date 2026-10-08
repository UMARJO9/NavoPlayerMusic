package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.data.TestPlaylists
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaylistRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class ObservePlaylistsUseCaseTest {

    private val tracks = FakeTrackRepository()
    private val playlists = FakePlaylistRepository(TestPlaylists.all)

    @Test
    fun `summaries count available tracks in update order`() = runTest {
        val observe = ObservePlaylistsUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(TestTracks.tracks)
            val summaries = awaitItem()
            assertEquals(listOf(TestPlaylists.empty.id, TestPlaylists.morning.id), summaries.map { it.id })
            val morning = summaries.last()
            assertEquals(2, morning.trackCount)
            assertEquals(TestTracks.longMix.durationMs + TestTracks.alpha.durationMs, morning.durationMs)
        }
    }

    @Test
    fun `library change re-emits summaries`() = runTest {
        val observe = ObservePlaylistsUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(TestTracks.tracks)
            assertEquals(2, awaitItem().last().trackCount)
            tracks.emit(listOf(TestTracks.alpha))
            assertEquals(1, awaitItem().last().trackCount)
        }
    }

    @Test
    fun `detail resolves tracks and reports missing`() = runTest {
        val observe = ObservePlaylistUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe(TestPlaylists.morning.id).test {
            tracks.emit(TestTracks.tracks)
            val detail = awaitItem()!!
            assertEquals(listOf(TestTracks.longMix, TestTracks.alpha), detail.tracks)
            assertEquals(1, detail.missingTrackCount)
        }
    }

    @Test
    fun `detail becomes null after delete`() = runTest {
        val observe = ObservePlaylistUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe(TestPlaylists.morning.id).test {
            tracks.emit(TestTracks.tracks)
            awaitItem()
            playlists.deletePlaylist(TestPlaylists.morning.id)
            assertNull(awaitItem())
        }
    }

    @Test
    fun `track errors propagate`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val observe = ObservePlaylistsUseCase(playlists, ObserveTracksUseCase(tracks, FakeSettingsRepository()), UnconfinedTestDispatcher(testScheduler))

        observe().test {
            assertEquals("scan failed", awaitError().message)
        }
    }
}
