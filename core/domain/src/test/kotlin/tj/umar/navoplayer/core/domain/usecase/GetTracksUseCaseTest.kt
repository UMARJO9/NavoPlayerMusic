package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class GetTracksUseCaseTest {

    private val repository = FakeTrackRepository()
    private val getTracks = GetTracksUseCase(repository)

    @Test
    fun `returns known tracks in requested order`() = runTest {
        repository.emit(listOf(TestTracks.alpha, TestTracks.beta))

        val result = getTracks(listOf(TestTracks.beta.id, 404L, TestTracks.alpha.id, TestTracks.beta.id))

        assertEquals(NavoResult.Success(listOf(TestTracks.beta, TestTracks.alpha)), result)
    }

    @Test
    fun `repository failure is an error`() = runTest {
        repository.getTracksError = IllegalStateException("denied")

        assertTrue(getTracks(listOf(TestTracks.alpha.id)) is NavoResult.Error)
    }
}
