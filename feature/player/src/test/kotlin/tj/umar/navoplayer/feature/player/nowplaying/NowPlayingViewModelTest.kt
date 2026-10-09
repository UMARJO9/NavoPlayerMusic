package tj.umar.navoplayer.feature.player.nowplaying

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
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
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.usecase.CancelSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.CycleRepeatModeUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveIsFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.ObservePlaybackStateUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.SeekToUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFavoriteUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToNextUseCase
import tj.umar.navoplayer.core.domain.usecase.SkipToPreviousUseCase
import tj.umar.navoplayer.core.domain.usecase.StartEndOfTrackSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.StartSleepTimerUseCase
import tj.umar.navoplayer.core.domain.usecase.TogglePlayPauseUseCase
import tj.umar.navoplayer.core.domain.usecase.ToggleShuffleUseCase
import tj.umar.navoplayer.core.testing.MainDispatcherRule
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.playback.FakeSleepTimerController
import tj.umar.navoplayer.core.testing.playback.SleepTimerCommand
import tj.umar.navoplayer.core.testing.repository.FakeFavoritesRepository
import kotlin.time.Duration.Companion.minutes

class NowPlayingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val controller = FakePlaybackController()
    private val favorites = FakeFavoritesRepository()
    private val sleepTimer = FakeSleepTimerController()
    private val viewModel = NowPlayingViewModel(
        observePlaybackState = ObservePlaybackStateUseCase(controller),
        observePlaybackProgress = ObservePlaybackProgressUseCase(controller),
        togglePlayPause = TogglePlayPauseUseCase(controller),
        skipToNext = SkipToNextUseCase(controller),
        skipToPrevious = SkipToPreviousUseCase(controller),
        seekTo = SeekToUseCase(controller),
        toggleShuffle = ToggleShuffleUseCase(controller),
        cycleRepeatMode = CycleRepeatModeUseCase(controller),
        observeIsFavorite = ObserveIsFavoriteUseCase(favorites),
        setFavorite = SetFavoriteUseCase(favorites),
        observeSleepTimer = ObserveSleepTimerUseCase(sleepTimer),
        startSleepTimer = StartSleepTimerUseCase(sleepTimer),
        startEndOfTrackSleepTimer = StartEndOfTrackSleepTimerUseCase(sleepTimer),
        cancelSleepTimer = CancelSleepTimerUseCase(sleepTimer),
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
    fun `more click changes nothing`() = runTest {
        startWith()
        val before = viewModel.state.value

        viewModel.onIntent(NowPlayingIntent.MoreClicked)

        assertEquals(before, viewModel.state.value)
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `queue click opens queue`() = runTest {
        startWith()

        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.QueueClicked)
            assertEquals(NowPlayingEffect.OpenQueue, awaitItem())
        }
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

    @Test
    fun `heart shows saved state of current track`() = runTest {
        favorites.addFavorite(TestTracks.alpha.id)

        startWith()

        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `advancing to next track moves forward`() = runTest {
        startWith()

        controller.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta, nextTrack = null))

        assertEquals(TrackChangeDirection.Forward, viewModel.state.value.trackChangeDirection)
    }

    @Test
    fun `previous click moves backward`() = runTest {
        startWith(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta, nextTrack = null))

        viewModel.onIntent(NowPlayingIntent.PreviousClicked)
        controller.state.emit(TestPlaybackStates.playingAlpha)

        assertEquals(TrackChangeDirection.Backward, viewModel.state.value.trackChangeDirection)
    }

    @Test
    fun `previous that restarted track does not reverse later advance`() = runTest {
        startWith()

        viewModel.onIntent(NowPlayingIntent.PreviousClicked)
        controller.state.emit(TestPlaybackStates.pausedAlpha)
        controller.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta, nextTrack = null))

        assertEquals(TrackChangeDirection.Forward, viewModel.state.value.trackChangeDirection)
    }

    @Test
    fun `next click after previous moves forward`() = runTest {
        startWith(TestPlaybackStates.playingAlpha.copy(nextTrack = null))

        viewModel.onIntent(NowPlayingIntent.PreviousClicked)
        viewModel.onIntent(NowPlayingIntent.NextClicked)
        controller.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta, nextTrack = null))

        assertEquals(TrackChangeDirection.Forward, viewModel.state.value.trackChangeDirection)
    }

    @Test
    fun `backward direction is kept while same track updates`() = runTest {
        startWith(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta, nextTrack = null))

        viewModel.onIntent(NowPlayingIntent.PreviousClicked)
        controller.state.emit(TestPlaybackStates.playingAlpha)
        controller.state.emit(TestPlaybackStates.pausedAlpha)

        assertEquals(TrackChangeDirection.Backward, viewModel.state.value.trackChangeDirection)
    }

    @Test
    fun `heart follows track changes`() = runTest {
        favorites.addFavorite(TestTracks.alpha.id)
        startWith()

        controller.state.emit(TestPlaybackStates.playingAlpha.copy(currentTrack = TestTracks.beta))
        assertFalse(viewModel.state.value.isFavorite)

        controller.state.emit(TestPlaybackStates.playingAlpha)
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite click saves and removes`() = runTest {
        startWith()

        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        assertEquals(listOf(TestTracks.alpha.id), favorites.current)
        assertTrue(viewModel.state.value.isFavorite)

        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        assertTrue(favorites.current.isEmpty())
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite failure shows message`() = runTest {
        startWith()
        favorites.writeError = IllegalStateException("locked")

        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
            assertEquals(NowPlayingEffect.ShowMessage(NowPlayingMessage.FavoriteFailed), awaitItem())
        }
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `stopped screen ignores favorite changes`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.ScreenStopped)

        favorites.addFavorite(TestTracks.alpha.id)

        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `restart does not show stale favorite`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        viewModel.onIntent(NowPlayingIntent.ScreenStopped)

        favorites.removeFavorite(TestTracks.alpha.id)
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)

        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `second tap while saving is ignored`() = runTest {
        startWith()
        val gate = CompletableDeferred<Unit>()
        favorites.writeGate = gate

        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        assertTrue(viewModel.state.value.isFavorite)
        viewModel.onIntent(NowPlayingIntent.FavoriteClicked)
        gate.complete(Unit)

        assertEquals(1, favorites.writeCalls)
        assertEquals(listOf(TestTracks.alpha.id), favorites.current)
        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `favorite observe error recovers after restart`() = runTest {
        favorites.observeError = IllegalStateException("db closed")
        startWith()

        favorites.observeError = null
        favorites.addFavorite(TestTracks.alpha.id)
        viewModel.onIntent(NowPlayingIntent.ScreenStopped)
        viewModel.onIntent(NowPlayingIntent.ScreenStarted)

        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `sleep timer click opens sheet and dismiss closes it`() = runTest {
        startWith()

        viewModel.onIntent(NowPlayingIntent.SleepTimerClicked)
        assertTrue(viewModel.state.value.isSleepTimerSheetVisible)

        viewModel.onIntent(NowPlayingIntent.SleepTimerSheetDismissed)
        assertFalse(viewModel.state.value.isSleepTimerSheetVisible)
    }

    @Test
    fun `selecting minutes starts countdown`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SleepTimerClicked)

        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.SleepTimerOptionSelected(SleepTimerOption.Minutes(15)))
            assertEquals(NowPlayingEffect.ShowMessage(NowPlayingMessage.SleepTimerSet(15)), awaitItem())
        }
        assertEquals(listOf(SleepTimerCommand.Start(15.minutes)), sleepTimer.commands)
        assertFalse(viewModel.state.value.isSleepTimerSheetVisible)
        assertEquals(SleepTimer.Countdown(900_000, 900_000), viewModel.state.value.sleepTimer)
    }

    @Test
    fun `selecting end of track starts end of track timer`() = runTest {
        startWith()

        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.SleepTimerOptionSelected(SleepTimerOption.EndOfTrack))
            assertEquals(NowPlayingEffect.ShowMessage(NowPlayingMessage.SleepTimerEndOfTrack), awaitItem())
        }
        assertEquals(SleepTimer.EndOfTrack, viewModel.state.value.sleepTimer)
    }

    @Test
    fun `cancel turns timer off`() = runTest {
        startWith()
        viewModel.onIntent(NowPlayingIntent.SleepTimerOptionSelected(SleepTimerOption.EndOfTrack))

        viewModel.effects.test {
            awaitItem()
            viewModel.onIntent(NowPlayingIntent.SleepTimerCancelClicked)
            assertEquals(NowPlayingEffect.ShowMessage(NowPlayingMessage.SleepTimerOff), awaitItem())
        }
        assertEquals(SleepTimerCommand.Cancel, sleepTimer.commands.last())
        assertEquals(SleepTimer.Off, viewModel.state.value.sleepTimer)
    }

    @Test
    fun `rejected start reports unavailable`() = runTest {
        startWith()
        sleepTimer.acceptStart = false

        viewModel.effects.test {
            viewModel.onIntent(NowPlayingIntent.SleepTimerOptionSelected(SleepTimerOption.Minutes(5)))
            assertEquals(NowPlayingEffect.ShowMessage(NowPlayingMessage.SleepTimerUnavailable), awaitItem())
        }
        assertEquals(SleepTimer.Off, viewModel.state.value.sleepTimer)
    }

    @Test
    fun `screen stopped stops observing timer`() = runTest {
        startWith()
        assertEquals(1, sleepTimer.subscribers)

        viewModel.onIntent(NowPlayingIntent.ScreenStopped)

        assertEquals(0, sleepTimer.subscribers)
    }

    @Test
    fun `selected option matches presets only`() {
        assertEquals(SleepTimerOption.Minutes(30), SleepTimer.Countdown(10_000, 1_800_000).selectedOption())
        assertEquals(SleepTimerOption.EndOfTrack, SleepTimer.EndOfTrack.selectedOption())
        assertEquals(null, SleepTimer.Countdown(10_000, 1_000).selectedOption())
        assertEquals(null, SleepTimer.Off.selectedOption())
    }
}
