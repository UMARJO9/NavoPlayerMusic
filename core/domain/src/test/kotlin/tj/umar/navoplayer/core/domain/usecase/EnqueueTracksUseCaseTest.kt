package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.domain.model.EnqueueOutcome
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.testing.data.TestPlaybackQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class EnqueueTracksUseCaseTest {

    private val repository = FakeTrackRepository()
    private val controller = FakePlaybackController()
    private val enqueue = EnqueueTracksUseCase(repository, controller)
    private val alpha = TestTracks.alpha
    private val beta = TestTracks.beta

    @Before
    fun setUp() = runTest {
        repository.emit(listOf(alpha, beta))
        controller.queue.emit(TestPlaybackQueues.alphaBetaAlpha)
    }

    @Test
    fun `play next enqueues tracks in requested order`() = runTest {
        val result = enqueue(listOf(beta.id, alpha.id), QueueInsertion.Next)

        assertEquals(NavoResult.Success(EnqueueOutcome.Enqueued(2, QueueInsertion.Next)), result)
        assertEquals(listOf(PlaybackCommand.Enqueue(listOf(beta, alpha), QueueInsertion.Next)), controller.commands)
    }

    @Test
    fun `add to queue appends`() = runTest {
        val result = enqueue(listOf(alpha.id), QueueInsertion.Last)

        assertEquals(NavoResult.Success(EnqueueOutcome.Enqueued(1, QueueInsertion.Last)), result)
        assertEquals(listOf(PlaybackCommand.Enqueue(listOf(alpha), QueueInsertion.Last)), controller.commands)
    }

    @Test
    fun `duplicate ids are enqueued once`() = runTest {
        enqueue(listOf(alpha.id, alpha.id), QueueInsertion.Last)

        assertEquals(listOf(PlaybackCommand.Enqueue(listOf(alpha), QueueInsertion.Last)), controller.commands)
    }

    @Test
    fun `empty queue starts playback`() = runTest {
        controller.queue.emit(PlaybackQueue.Empty)

        val result = enqueue(listOf(alpha.id, beta.id), QueueInsertion.Next)

        assertEquals(NavoResult.Success(EnqueueOutcome.StartedPlayback(2)), result)
        assertEquals(listOf(PlaybackCommand.Play(listOf(alpha, beta), 0, PlaybackSource.Queue)), controller.commands)
    }

    @Test
    fun `no ids or unknown ids add nothing`() = runTest {
        assertEquals(NavoResult.Success(EnqueueOutcome.NothingToAdd), enqueue(emptyList(), QueueInsertion.Next))
        assertEquals(NavoResult.Success(EnqueueOutcome.NothingToAdd), enqueue(listOf(404L), QueueInsertion.Next))
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `repository failure is an error`() = runTest {
        repository.getTracksError = IllegalStateException("denied")

        val result = enqueue(listOf(alpha.id), QueueInsertion.Next)

        assertTrue(result is NavoResult.Error)
        assertTrue(controller.commands.isEmpty())
    }
}
