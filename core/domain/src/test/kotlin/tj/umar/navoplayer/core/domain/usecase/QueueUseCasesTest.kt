package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.testing.data.TestPlaybackQueues
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class QueueUseCasesTest {

    private val controller = FakePlaybackController()
    private val queue = TestPlaybackQueues.alphaBetaAlpha

    @Test
    fun `observe queue passes controller queue through`() = runTest {
        controller.queue.emit(queue)

        ObservePlaybackQueueUseCase(controller)().test {
            assertEquals(queue, awaitItem())
        }
    }

    @Test
    fun `skip delegates to controller`() = runTest {
        SkipToQueueItemUseCase(controller)(QueueItemId("q2"))

        assertEquals(listOf(PlaybackCommand.SkipToQueueItem(QueueItemId("q2"))), controller.commands)
    }

    @Test
    fun `remove delegates for other item`() = runTest {
        controller.queue.emit(queue)

        RemoveQueueItemUseCase(controller)(QueueItemId("q2"))

        assertEquals(listOf(PlaybackCommand.RemoveQueueItem(QueueItemId("q2"))), controller.commands)
    }

    @Test
    fun `remove ignores current and unknown items`() = runTest {
        controller.queue.emit(queue)
        val remove = RemoveQueueItemUseCase(controller)

        remove(QueueItemId("q1"))
        remove(QueueItemId("missing"))

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `move delegates with target index`() = runTest {
        controller.queue.emit(queue)

        MoveQueueItemUseCase(controller)(QueueItemId("q3"), 1)

        assertEquals(listOf(PlaybackCommand.MoveQueueItem(QueueItemId("q3"), 1)), controller.commands)
    }

    @Test
    fun `move coerces out of range target`() = runTest {
        controller.queue.emit(queue)
        val move = MoveQueueItemUseCase(controller)

        move(QueueItemId("q1"), 99)
        move(QueueItemId("q3"), -4)

        assertEquals(
            listOf(
                PlaybackCommand.MoveQueueItem(QueueItemId("q1"), 2),
                PlaybackCommand.MoveQueueItem(QueueItemId("q3"), 0),
            ),
            controller.commands,
        )
    }

    @Test
    fun `move ignores unknown item and same position`() = runTest {
        controller.queue.emit(queue)
        val move = MoveQueueItemUseCase(controller)

        move(QueueItemId("missing"), 0)
        move(QueueItemId("q2"), 1)

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `remove and move report rejection`() = runTest {
        controller.queue.emit(queue)
        controller.rejectQueueCommands = true

        assertFalse(RemoveQueueItemUseCase(controller)(QueueItemId("q2")))
        assertFalse(MoveQueueItemUseCase(controller)(QueueItemId("q3"), 0))
    }

    @Test
    fun `remove reports ignored current item and move reports unchanged position`() = runTest {
        controller.queue.emit(queue)

        assertFalse(RemoveQueueItemUseCase(controller)(QueueItemId("q1")))
        assertTrue(MoveQueueItemUseCase(controller)(QueueItemId("q2"), 1))
        assertTrue(RemoveQueueItemUseCase(controller)(QueueItemId("q2")))
    }
}
