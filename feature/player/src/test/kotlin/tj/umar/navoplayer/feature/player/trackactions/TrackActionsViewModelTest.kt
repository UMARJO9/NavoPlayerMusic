package tj.umar.navoplayer.feature.player.trackactions

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.usecase.EnqueueTracksUseCase
import tj.umar.navoplayer.core.domain.usecase.GetTracksUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class TrackActionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTrackRepository()
    private val controller = FakePlaybackController()
    private val viewModel = TrackActionsViewModel(
        getTracks = GetTracksUseCase(repository),
        enqueueTracks = EnqueueTracksUseCase(repository, controller),
    )

    private val alpha = TestTracks.alpha
    private val beta = TestTracks.beta

    private suspend fun open(vararg ids: Long, token: Long = 1, queue: PlaybackQueue = TestPlaybackQueues.alphaBetaAlpha) {
        repository.emit(listOf(alpha, beta))
        controller.queue.emit(queue)
        viewModel.onIntent(TrackActionsIntent.Opened(TrackActionsRequest(token, ids.toList())))
    }

    @Test
    fun `opened loads selected track`() = runTest {
        open(alpha.id)

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(alpha, state.track)
        assertEquals(listOf(alpha.id), state.trackIds)
    }

    @Test
    fun `unknown ids load no tracks`() = runTest {
        open(404L)

        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.tracks.isEmpty())
    }

    @Test
    fun `play next enqueues after current`() = runTest {
        open(alpha.id)

        viewModel.effects.test {
            viewModel.onIntent(TrackActionsIntent.PlayNextClicked)
            assertEquals(TrackActionsEffect.Enqueued(1, QueueInsertion.Next, 1), awaitItem())
        }
        assertEquals(listOf(PlaybackCommand.Enqueue(listOf(alpha), QueueInsertion.Next)), controller.commands)
        assertFalse(viewModel.state.value.isWorking)
    }

    @Test
    fun `add to queue appends`() = runTest {
        open(alpha.id, beta.id)

        viewModel.effects.test {
            viewModel.onIntent(TrackActionsIntent.AddToQueueClicked)
            assertEquals(TrackActionsEffect.Enqueued(1, QueueInsertion.Last, 2), awaitItem())
        }
        assertEquals(listOf(PlaybackCommand.Enqueue(listOf(alpha, beta), QueueInsertion.Last)), controller.commands)
    }

    @Test
    fun `empty queue starts playback`() = runTest {
        open(beta.id, queue = PlaybackQueue.Empty)

        viewModel.effects.test {
            viewModel.onIntent(TrackActionsIntent.PlayNextClicked)
            assertEquals(TrackActionsEffect.StartedPlayback(1), awaitItem())
        }
        assertEquals(listOf(PlaybackCommand.Play(listOf(beta), 0, PlaybackSource.Queue)), controller.commands)
    }

    @Test
    fun `missing tracks or errors fail`() = runTest {
        open(404L)

        viewModel.effects.test {
            viewModel.onIntent(TrackActionsIntent.PlayNextClicked)
            assertEquals(TrackActionsEffect.Failed(1), awaitItem())

            repository.getTracksError = IllegalStateException("denied")
            viewModel.onIntent(TrackActionsIntent.AddToQueueClicked)
            assertEquals(TrackActionsEffect.Failed(1), awaitItem())
        }
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `add to playlist hands over track ids`() = runTest {
        open(alpha.id, beta.id)

        viewModel.effects.test {
            viewModel.onIntent(TrackActionsIntent.AddToPlaylistClicked)
            assertEquals(TrackActionsEffect.OpenAddToPlaylist(1, listOf(alpha.id, beta.id)), awaitItem())
        }
    }

    @Test
    fun `new request resets state`() = runTest {
        open(alpha.id, token = 1)

        open(beta.id, token = 2)

        assertEquals(2L, viewModel.state.value.token)
        assertEquals(beta, viewModel.state.value.track)
    }

    @Test
    fun `same request does not reload`() = runTest {
        open(alpha.id, token = 1)
        repository.getTracksError = IllegalStateException("denied")

        viewModel.onIntent(TrackActionsIntent.Opened(TrackActionsRequest(1, listOf(alpha.id))))

        assertEquals(alpha, viewModel.state.value.track)
    }
}
