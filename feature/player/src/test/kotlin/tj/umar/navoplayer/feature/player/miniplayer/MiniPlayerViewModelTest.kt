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
    fun `progress becomes fraction`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.ScreenStarted)
        controller.progress.emit(PlaybackProgress(positionMs = 50_000, durationMs = 200_000))

        assertEquals(0.25f, viewModel.state.value.progress)
    }

    @Test
    fun `play pause click toggles playback`() = runTest {
        viewModel.onIntent(MiniPlayerIntent.PlayPauseClicked)

        assertEquals(listOf(PlaybackCommand.TogglePlayPause), controller.commands)
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
