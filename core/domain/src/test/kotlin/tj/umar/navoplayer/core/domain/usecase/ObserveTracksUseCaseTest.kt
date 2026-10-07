package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class ObserveTracksUseCaseTest {

    private val repository = FakeTrackRepository()
    private val observeTracks = ObserveTracksUseCase(repository)

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
            assertEquals(failure, awaitError())
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
}
