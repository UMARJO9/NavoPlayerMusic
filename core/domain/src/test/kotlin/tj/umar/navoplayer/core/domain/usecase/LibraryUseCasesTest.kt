package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class LibraryUseCasesTest {

    private val repository = FakeTrackRepository()

    @Test
    fun `library emission builds all lists`() = runTest {
        val observeLibrary = ObserveLibraryUseCase(ObserveTracksUseCase(repository), UnconfinedTestDispatcher(testScheduler))

        observeLibrary().test {
            repository.emit(TestTracks.library)
            val content = awaitItem()
            assertEquals(TestTracks.library, content.tracks)
            assertEquals(4, content.albums.size)
            assertEquals(5, content.artists.size)
            assertEquals(4, content.folders.size)

            repository.emit(listOf(TestTracks.alpha))
            assertEquals(1, awaitItem().albums.size)
        }
    }

    @Test
    fun `library propagates errors`() = runTest {
        repository.error = IllegalStateException("scan failed")
        val observeLibrary = ObserveLibraryUseCase(ObserveTracksUseCase(repository), UnconfinedTestDispatcher(testScheduler))

        observeLibrary().test {
            assertEquals("scan failed", awaitError().message)
        }
    }

    @Test
    fun `group observation follows the group and suppresses duplicates`() = runTest {
        val key = TrackGroupKey(TrackGroupType.Album, 10, null)
        val observeGroup = ObserveTrackGroupUseCase(ObserveTracksUseCase(repository), UnconfinedTestDispatcher(testScheduler))

        observeGroup(key).test {
            repository.emit(TestTracks.library)
            assertEquals(listOf(TestTracks.alpha, TestTracks.alphaTwo), awaitItem()?.tracks)

            repository.emit(TestTracks.library)
            expectNoEvents()

            repository.emit(listOf(TestTracks.longMix))
            assertNull(awaitItem())
        }
    }
}
