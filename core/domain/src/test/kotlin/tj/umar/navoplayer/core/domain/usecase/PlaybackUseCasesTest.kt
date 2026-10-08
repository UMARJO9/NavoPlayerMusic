package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.testing.data.TestPlaybackStates
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class PlaybackUseCasesTest {

    private val controller = FakePlaybackController()
    private val source = PlaybackSource.AllTracks

    @Test
    fun `observe state passes controller state through`() = runTest {
        controller.state.emit(TestPlaybackStates.playingAlpha)

        ObservePlaybackStateUseCase(controller)().test {
            assertEquals(TestPlaybackStates.playingAlpha, awaitItem())
        }
    }

    @Test
    fun `observe progress passes controller progress through`() = runTest {
        val progress = PlaybackProgress(positionMs = 1_000, durationMs = 4_000)
        controller.progress.emit(progress)

        ObservePlaybackProgressUseCase(controller)().test {
            assertEquals(progress, awaitItem())
        }
    }

    @Test
    fun `play forwards queue and index`() = runTest {
        PlayTracksUseCase(controller)(TestTracks.tracks, 1, source)

        assertEquals(listOf(PlaybackCommand.Play(TestTracks.tracks, 1, source)), controller.commands)
    }

    @Test
    fun `play clamps start index into range`() = runTest {
        val play = PlayTracksUseCase(controller)

        play(TestTracks.tracks, -3, source)
        play(TestTracks.tracks, 99, source)

        assertEquals(
            listOf(
                PlaybackCommand.Play(TestTracks.tracks, 0, source),
                PlaybackCommand.Play(TestTracks.tracks, TestTracks.tracks.lastIndex, source),
            ),
            controller.commands,
        )
    }

    @Test
    fun `play with empty queue does nothing`() = runTest {
        PlayTracksUseCase(controller)(emptyList(), 0, source)

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `shuffle play forwards queue`() = runTest {
        ShufflePlayTracksUseCase(controller)(TestTracks.tracks, source)

        assertEquals(listOf(PlaybackCommand.PlayShuffled(TestTracks.tracks, source)), controller.commands)
    }

    @Test
    fun `shuffle play with empty queue does nothing`() = runTest {
        ShufflePlayTracksUseCase(controller)(emptyList(), source)

        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `simple commands are forwarded`() = runTest {
        TogglePlayPauseUseCase(controller)()
        SkipToNextUseCase(controller)()
        SkipToPreviousUseCase(controller)()

        assertEquals(
            listOf(PlaybackCommand.TogglePlayPause, PlaybackCommand.SkipToNext, PlaybackCommand.SkipToPrevious),
            controller.commands,
        )
    }

    @Test
    fun `seek clamps negative position to zero`() = runTest {
        val seek = SeekToUseCase(controller)

        seek(-500)
        seek(2_000)

        assertEquals(listOf(PlaybackCommand.SeekTo(0), PlaybackCommand.SeekTo(2_000)), controller.commands)
    }

    @Test
    fun `toggle shuffle flips current value`() = runTest {
        val toggle = ToggleShuffleUseCase(controller)

        controller.state.emit(TestPlaybackStates.playingAlpha)
        toggle()
        controller.state.emit(TestPlaybackStates.playingAlpha.copy(shuffleEnabled = true))
        toggle()

        assertEquals(listOf(PlaybackCommand.SetShuffle(true), PlaybackCommand.SetShuffle(false)), controller.commands)
    }

    @Test
    fun `cycle repeat moves to next mode`() = runTest {
        val cycle = CycleRepeatModeUseCase(controller)

        RepeatMode.entries.forEach { mode ->
            controller.state.emit(TestPlaybackStates.playingAlpha.copy(repeatMode = mode))
            cycle()
        }

        assertEquals(
            listOf(
                PlaybackCommand.SetRepeat(RepeatMode.All),
                PlaybackCommand.SetRepeat(RepeatMode.One),
                PlaybackCommand.SetRepeat(RepeatMode.Off),
            ),
            controller.commands,
        )
    }
}
