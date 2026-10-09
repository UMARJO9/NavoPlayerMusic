package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeSettingsRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class ObserveSortedLibraryUseCaseTest {

    private val tracks = FakeTrackRepository()
    private val settings = FakeSettingsRepository()

    private fun TestScope.observe(): ObserveSortedLibraryUseCase {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return ObserveSortedLibraryUseCase(
            ObserveLibraryUseCase(ObserveTracksUseCase(tracks, settings), dispatcher),
            settings,
            dispatcher,
        )
    }

    @Test
    fun `default sort keeps repository order`() = runTest {
        observe()().test {
            tracks.emit(TestTracks.tracks)
            val library = awaitItem()
            assertEquals(TestTracks.tracks, library.content.tracks)
            assertEquals(TrackSort.Default, library.trackSort)
        }
    }

    @Test
    fun `track sort change re-sorts tracks`() = runTest {
        observe()().test {
            tracks.emit(TestTracks.tracks)
            awaitItem()
            settings.setTrackSortField(TrackSortField.Duration)
            settings.setTrackSortDirection(SortDirection.Descending)
            skipItems(1)
            val library = awaitItem()
            assertEquals(TestTracks.tracks.sortedByDescending { it.durationMs }, library.content.tracks)
            assertEquals(TrackSort(TrackSortField.Duration, SortDirection.Descending), library.trackSort)
        }
    }

    @Test
    fun `group sort change re-sorts groups`() = runTest {
        observe()().test {
            tracks.emit(TestTracks.library)
            val before = awaitItem().content.albums
            settings.setGroupSortDirection(SortDirection.Descending)
            val after = awaitItem()
            assertEquals(GroupSort(GroupSortField.Name, SortDirection.Descending), after.groupSort)
            assertEquals(before.filterNot { it.key.isUnknown }.reversed(), after.content.albums.filterNot { it.key.isUnknown })
        }
    }

    @Test
    fun `unrelated setting does not emit`() = runTest {
        observe()().test {
            tracks.emit(TestTracks.tracks)
            awaitItem()
            settings.emit(UserSettings(pauseOnHeadphonesDisconnect = false))
            expectNoEvents()
        }
    }

    @Test
    fun `track errors propagate`() = runTest {
        tracks.error = IllegalStateException("scan failed")

        observe()().test {
            assertEquals("scan failed", awaitError().message)
        }
    }
}
