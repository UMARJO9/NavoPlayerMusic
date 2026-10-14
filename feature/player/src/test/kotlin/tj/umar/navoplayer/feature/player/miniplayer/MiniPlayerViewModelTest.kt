package tj.umar.navoplayer.feature.player.miniplayer

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToNextUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class MiniPlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()
    private val viewModel = MiniPlayerViewModel(
        observePlaybackState = ObservePlaybackStateUseCase(controller),
        observePlaybackProgress = ObservePlaybackProgressUseCase(controller),
        togglePlayPause = TogglePlayPauseUseCase(controller),
        skipToNext = SkipToNextUseCase(controller),
    )

    @Test
    fun `initial state has no track`() {
        assertNull(viewModel.state.value.track)
        assertFalse(viewModel.state.value.isPlaying)
    }

    @Test
    fun `maps current track and playing flag`() = runTest {
        controller.state.emit(TestPlaybackStates.playingAlpha)
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)

        assertEquals(TestTracks.alpha, viewModel.state.value.track)
        assertTrue(viewModel.state.value.isPlaying)
    }

    @Test
    fun `progress maps position and duration`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.progress.emit(PlaybackProgress(positionMs = 50_000, durationMs = 200_000))

        assertEquals(50_000L, viewModel.state.value.positionMs)
        assertEquals(200_000L, viewModel.state.value.durationMs)
    }

    @Test
    fun `maps has next from next track`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.state.emit(TestPlaybackStates.playingAlpha)
        assertTrue(viewModel.state.value.hasNext)

        controller.state.emit(TestPlaybackStates.playingAlpha.copy(nextTrack = null))
        assertFalse(viewModel.state.value.hasNext)
    }

    @Test
    fun `track change resets position`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.state.emit(TestPlaybackStates.playingAlpha)
        controller.progress.emit(PlaybackProgress(positionMs = 150_000, durationMs = 200_000))

        controller.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta))

        assertEquals(0L, viewModel.state.value.positionMs)
        assertEquals(TestTracks.beta.durationMs, viewModel.state.value.durationMs)
    }

    @Test
    fun `first track keeps position emitted before it`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.progress.emit(PlaybackProgress(positionMs = 90_000, durationMs = 200_000))

        controller.state.emit(TestPlaybackStates.pausedAlpha)

        assertEquals(90_000L, viewModel.state.value.positionMs)
        assertEquals(200_000L, viewModel.state.value.durationMs)
    }

    @Test
    fun `play pause click toggles playback`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.PlayPauseClicked)

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), controller.commands)
    }

    @Test
    fun `next click skips when has next`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.state.emit(TestPlaybackStates.playingAlpha)

        viewModel.onIntent(MiniPlayerIntent.NextClicked)

        assertEquals(listOf(PlaybackCommand.SkipToNext), controller.commands)
    }

    @Test
    fun `next click without next does nothing`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.state.emit(TestPlaybackStates.playingAlpha.copy(nextTrack = null))

        viewModel.onIntent(MiniPlayerIntent.NextClicked)

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `open click with track opens now playing`() = runTest {
        controller.state.emit(TestPlaybackStates.pausedAlpha)
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)

        viewModel.effects.test {
            viewModel.onIntent(MiniPlayerIntent.OpenClicked)
            assertEquals(MiniPlayerEffect.OpenNowPlaying, awaitItem())
        }
    }

    @Test
    fun `open click without track does nothing`() = runTest {
        controller.state.emit(PlaybackState.Empty)
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)

        viewModel.effects.test {
            viewModel.onIntent(MiniPlayerIntent.OpenClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `screen stop unsubscribes`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        viewModel.onIntent(MiniPlayerIntent.ScreenStopped)

        assertEquals(0, controller.stateSubscribers)
        assertEquals(0, controller.progressSubscribers)
    }
}
