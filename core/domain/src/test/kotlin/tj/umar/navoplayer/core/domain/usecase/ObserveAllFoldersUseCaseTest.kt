package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class ObserveAllFoldersUseCaseTest {

    private val tracks = FakeTrackRepository()
    private val settings = FakeSettingsRepository(
        UserSettings(minTrackDuration = MinTrackDuration.SixtySeconds, excludedFolders = setOf("Music/Navo")),
    )

    @Test
    fun `folders include excluded ones with raw counts`() = runTest {
        val observe = ObserveAllFoldersUseCase(tracks, settings, UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(TestTracks.tracks)
            val folders = awaitItem()
            assertEquals(listOf("Music/Mixes", "Music/Navo"), folders.map { it.path })
            assertEquals(listOf(false, true), folders.map { it.isExcluded })
            assertEquals(listOf(1, 1), folders.map { it.trackCount })
        }
        assertEquals(1, tracks.observeCalls)
    }

    @Test
    fun `exclusion change updates flags`() = runTest {
        val observe = ObserveAllFoldersUseCase(tracks, settings, UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(TestTracks.tracks)
            awaitItem()
            settings.setFolderExcluded("Music/Navo", false)
            assertEquals(listOf(false, false), awaitItem().map { it.isExcluded })
        }
    }

    @Test
    fun `errors propagate`() = runTest {
        tracks.error = IllegalStateException("scan failed")
        val observe = ObserveAllFoldersUseCase(tracks, settings, UnconfinedTestDispatcher(testScheduler))

        observe().test {
            assertEquals("scan failed", awaitError().message)
        }
    }
}
