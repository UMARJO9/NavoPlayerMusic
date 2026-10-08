package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class ObserveTracksUseCaseTest {

    private val repository = FakeTrackRepository()
    private val settings = FakeSettingsRepository()
    private val observeTracks = ObserveTracksUseCase(repository, settings)

    @Test
    fun `emits tracks from repository`() = runTest {
        repository.emit(TestTracks.tracks)

        observeTracks().test {
            assertEquals(TestTracks.tracks, awaitItem())
        }
    }

    @Test
    fun `reflects later repository emissions`() = runTest {
        observeTracks().test {
            repository.emit(listOf(TestTracks.alpha))
            assertEquals(listOf(TestTracks.alpha), awaitItem())
            repository.emit(TestTracks.tracks)
            assertEquals(TestTracks.tracks, awaitItem())
        }
    }

    @Test
    fun `propagates repository error`() = runTest {
        val failure = IllegalStateException("scan failed")
        repository.error = failure

        observeTracks().test {
            assertEquals(failure.message, awaitError().message)
        }
    }

    @Test
    fun `subscribes to repository once per collection`() = runTest {
        repository.emit(TestTracks.tracks)

        observeTracks().test {
            awaitItem()
        }

        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `hides short tracks and excluded folders`() = runTest {
        repository.emit(TestTracks.tracks)
        settings.emit(UserSettings(minTrackDuration = MinTrackDuration.SixtySeconds))

        observeTracks().test {
            assertEquals(TestTracks.tracks.filter { it.durationMs >= 60_000 }, awaitItem())
            settings.emit(UserSettings(excludedFolders = setOf(TestTracks.alpha.folderPath!!)))
            assertEquals(TestTracks.tracks.filter { it.folderPath != TestTracks.alpha.folderPath }, awaitItem())
        }
        assertEquals(1, repository.observeCalls)
    }

    @Test
    fun `headphones toggle does not re-emit tracks`() = runTest {
        repository.emit(TestTracks.tracks)

        observeTracks().test {
            awaitItem()
            settings.emit(UserSettings(pauseOnHeadphonesDisconnect = false))
            expectNoEvents()
        }
    }

    @Test
    fun `settings error still emits tracks`() = runTest {
        settings.observeError = IllegalStateException("broken")
        repository.emit(TestTracks.tracks)

        observeTracks().test {
            assertEquals(TestTracks.tracks, awaitItem())
        }
    }
}
