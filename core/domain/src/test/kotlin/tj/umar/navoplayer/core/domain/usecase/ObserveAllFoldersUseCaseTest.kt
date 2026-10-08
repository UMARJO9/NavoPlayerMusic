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

    @Test
    fun `folder with only short tracks is still listed with raw count`() = runTest {
        val clip = TestTracks.alpha.copy(id = 70, durationMs = 5_000, folderPath = "Recordings")
        val observe = ObserveAllFoldersUseCase(tracks, settings, UnconfinedTestDispatcher(testScheduler))

        observe().test {
            tracks.emit(listOf(clip))
            assertEquals(listOf("Music/Navo", "Recordings"), awaitItem().map { it.path })
        }
    }
}
