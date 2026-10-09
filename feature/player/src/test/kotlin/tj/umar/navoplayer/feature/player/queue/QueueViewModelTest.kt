package tj.umar.navoplayer.feature.player.queue

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.usecase.MoveQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.RemoveQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToQueueItemUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackQueues
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class QueueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()
    private val viewModel = QueueViewModel(
        observePlaybackQueue = ObservePlaybackQueueUseCase(controller),
        observePlaybackState = ObservePlaybackStateUseCase(controller),
        skipToQueueItem = SkipToQueueItemUseCase(controller),
        removeQueueItem = RemoveQueueItemUseCase(controller),
        moveQueueItem = MoveQueueItemUseCase(controller),
        togglePlayPause = TogglePlayPauseUseCase(controller),
    )

    private val queue = TestPlaybackQueues.alphaBetaAlpha
    private val first = TestPlaybackQueues.alphaItem
    private val second = TestPlaybackQueues.betaItem
    private val third = TestPlaybackQueues.alphaAgainItem

    private suspend fun start(
        playback: PlaybackState = TestPlaybackStates.playingAlpha,
    ) {
        controller.queue.emit(queue)
        controller.state.emit(playback)
        viewModel.onIntent(QueueIntent.ScreenStarted)
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.state.value.isLoading)
    }

    @Test
    fun `screen started maps queue and playback`() = runTest {
        start(TestPlaybackStates.pausedAlpha.copy(shuffleEnabled = true, source = PlaybackSource.Favorites))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(queue.items, state.items)
        assertEquals(first.id, state.currentItemId)
        assertEquals(0, state.currentIndex)
        assertFalse(state.isPlaying)
        assertTrue(state.shuffleEnabled)
        assertEquals(PlaybackSource.Favorites, state.source)
    }

    @Test
    fun `screen stopped unsubscribes`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.ScreenStopped)

        assertEquals(0, controller.queueSubscribers)
        assertEquals(0, controller.stateSubscribers)
    }

    @Test
    fun `empty queue closes once`() = runTest {
        viewModel.effects.test {
            start()
            controller.queue.emit(PlaybackQueue.Empty)
            assertEquals(QueueEffect.Close, awaitItem())

            controller.queue.emit(PlaybackQueue.Empty)
            viewModel.onIntent(QueueIntent.CloseClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `clicking other item skips to it`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.ItemClicked(second.id))

        assertEquals(listOf(PlaybackCommand.SkipToQueueItem(second.id)), controller.commands)
    }

    @Test
    fun `clicking current item resumes only when paused`() = runTest {
        start(TestPlaybackStates.playingAlpha)
        viewModel.onIntent(QueueIntent.ItemClicked(first.id))
        assertTrue(controller.commands.isEmpty())

        controller.state.emit(TestPlaybackStates.pausedAlpha)
        viewModel.onIntent(QueueIntent.ItemClicked(first.id))

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), controller.commands)
    }

    @Test
    fun `remove drops item optimistically and sends command`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.RemoveClicked(second.id))

        assertEquals(listOf(first, third), viewModel.state.value.items)
        assertEquals(listOf(PlaybackCommand.RemoveQueueItem(second.id)), controller.commands)
    }

    @Test
    fun `current and unknown items are not removed`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.RemoveClicked(first.id))
        viewModel.onIntent(QueueIntent.RemoveClicked(QueueItemId("missing")))

        assertEquals(queue.items, viewModel.state.value.items)
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `move reorders optimistically and sends command`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.MoveItem(third.id, 0))

        assertEquals(listOf(third, first, second), viewModel.state.value.items)
        assertEquals(listOf(PlaybackCommand.MoveQueueItem(third.id, 0)), controller.commands)
    }

    @Test
    fun `move to same position or unknown item does nothing`() = runTest {
        start()

        viewModel.onIntent(QueueIntent.MoveItem(second.id, 1))
        viewModel.onIntent(QueueIntent.MoveItem(QueueItemId("missing"), 0))

        assertEquals(queue.items, viewModel.state.value.items)
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `next queue emission replaces optimistic state`() = runTest {
        start()
        viewModel.onIntent(QueueIntent.RemoveClicked(second.id))

        controller.queue.emit(queue.copy(currentIndex = 1))

        assertEquals(queue.items, viewModel.state.value.items)
        assertEquals(second.id, viewModel.state.value.currentItemId)
    }

    @Test
    fun `close clicked closes`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(QueueIntent.CloseClicked)
            assertEquals(QueueEffect.Close, awaitItem())
        }
    }
}
