package tj.umar.navoplayer.feature.player.nowplaying

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.usecase.CycleRepeatModeUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.SeekToUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToNextUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToPreviousUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.domain.usecase.ToggleShuffleUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class NowPlayingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()
    private val viewModel = NowPlayingViewModel(
        observePlaybackState = ObservePlaybackStateUseCase(controller),
        observePlaybackProgress = ObservePlaybackProgressUseCase(controller),
        togglePlayPause = TogglePlayPauseUseCase(controller),
        skipToNext = SkipToNextUseCase(controller),
        skipToPrevious = SkipToPreviousUseCase(controller),
        seekTo = SeekToUseCase(controller),
        toggleShuffle = ToggleShuffleUseCase(controller),
        cycleRepeatMode = CycleRepeatModeUseCase(controller),
    )

    private suspend fun startWith(state: PlaybackState = TestPlaybackStates.playingAlpha) {
        controller.state.emit(state)
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)
    }

    @Test
    fun `initial state is loading`() {
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.track)
    }

    @Test
    fun `maps playback state`() = runTest {
        startWith(TestPlaybackStates.playingAlpha.copy(shuffleEnabled = true, repeatMode = RepeatMode.One))

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(TestTracks.alpha, state.track)
        assertEquals(TestTracks.beta, state.nextTrack)
        assertEquals(PlaybackSource.AllTracks, state.source)
        assertTrue(state.isPlaying)
        assertTrue(state.shuffleEnabled)
        assertEquals(RepeatMode.One, state.repeatMode)
    }

    @Test
    fun `missing track collapses once`() = runTest {
        viewModel.effects.test {
            startWith(PlaybackState.Empty)
            assertEquals(NowPlayingEffect.Collapse, awaitItem())
            controller.state.emit(PlaybackState.Empty)
            viewModel.onIntent(NowPlayingIntent.CollapseClicked)
            expectNoEvents()
        }
    }

    @Test
    fun `progress updates position and duration`() = runTest {
        startWith()
        controller.progress.emit(PlaybackProgress(positionMs = 30_000, durationMs = 185_000))

        assertEquals(30_000L, viewModel.state.value.positionMs)
        assertEquals(185_000L, viewModel.state.value.durationMs)
    }

    @Test
    fun `seek preview is kept while ticks arrive`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SeekChanged(90_000))
        controller.progress.emit(PlaybackProgress(positionMs = 31_000, durationMs = 185_000))

        val state = viewModel.state.value
        assertEquals(90_000L, state.seekPreviewMs)
        assertEquals(90_000L, state.displayedPositionMs)
    }

    @Test
    fun `seek finish seeks and clears preview`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SeekChanged(90_000))
        viewModel.onIntent(NowPlayingIntent.SeekFinished)

        assertNull(viewModel.state.value.seekPreviewMs)
        assertEquals(90_000L, viewModel.state.value.positionMs)
        assertEquals(listOf(PlaybackCommand.SeekTo(90_000)), controller.commands)
    }

    @Test
    fun `seek finish without preview does nothing`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SeekFinished)

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `control buttons send commands`() = runTest {
        startWith()

        viewModel.onIntent(NowPlayingIntent.PlayPauseClicked)
        viewModel.onIntent(NowPlayingIntent.NextClicked)
        viewModel.onIntent(NowPlayingIntent.PreviousClicked)
        viewModel.onIntent(NowPlayingIntent.ShuffleClicked)
        viewModel.onIntent(NowPlayingIntent.RepeatClicked)

        assertEquals(
            listOf(
                PlaybackCommand.TogglePlayPause,
                PlaybackCommand.SkipToNext,
                PlaybackCommand.SkipToPrevious,
                PlaybackCommand.SetShuffle(true),
                PlaybackCommand.SetRepeat(RepeatMode.All),
            ),
            controller.commands,
        )
    }

    @Test
    fun `favorite toggles for current track`() = runTest {
        startWith()

        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        assertTrue(viewModel.state.value.isFavorite)

        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `collapse click sends collapse`() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.CollapseClicked)
            assertEquals(NowPlayingEffect.Collapse, awaitItem())
        }
    }

    @Test
    fun `screen stop unsubscribes from playback`() = runTest {
        startWith()
        assertEquals(1, controller.stateSubscribers)
        assertEquals(1, controller.progressSubscribers)

        viewModel.onIntent(NowPlayingIntent.ScreenStopped)

        assertEquals(0, controller.stateSubscribers)
        assertEquals(0, controller.progressSubscribers)
    }

    @Test
    fun `placeholder buttons change nothing`() = runTest {
        startWith()
        val before = viewModel.state.value

        viewModel.onIntent(NowPlayingIntent.MoreClicked)
        viewModel.onIntent(NowPlayingIntent.QueueClicked)
        viewModel.onIntent(NowPlayingIntent.SleepTimerClicked)

        assertEquals(before, viewModel.state.value)
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `stale tick after seek does not jump back`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SeekChanged(90_000))
        viewModel.onIntent(NowPlayingIntent.SeekFinished)

        controller.progress.emit(PlaybackProgress(positionMs = 31_000, durationMs = 185_000))
        assertEquals(90_000L, viewModel.state.value.positionMs)

        controller.progress.emit(PlaybackProgress(positionMs = 90_400, durationMs = 185_000))
        assertEquals(90_400L, viewModel.state.value.positionMs)
    }

    @Test
    fun `progress resumes after too many stale ticks`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SeekChanged(90_000))
        viewModel.onIntent(NowPlayingIntent.SeekFinished)

        (1..5).forEach { tick ->
            controller.progress.emit(PlaybackProgress(positionMs = 31_000L + tick, durationMs = 185_000))
        }

        assertEquals(31_005L, viewModel.state.value.positionMs)
    }

    @Test
    fun `album source passes through`() = runTest {
        startWith(TestPlaybackStates.playingAlpha.copy(source = PlaybackSource.Album("First")))

        assertEquals(PlaybackSource.Album("First"), viewModel.state.value.source)
    }
}
