package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.model.TransportOutcome
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakeNowPlayingMonitor
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand

class NowPlayingUseCasesTest {

    private val monitor = FakeNowPlayingMonitor()
    private val controller = FakePlaybackController()
    private val send = SendTransportCommandUseCase(monitor, controller)

    @Test
    fun `observe passes monitor values through`() = runTest {
        ObserveNowPlayingUseCase(monitor)().test {
            assertEquals(NowPlaying.Idle, awaitItem())
            monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = true)
            assertEquals(NowPlaying(TestTracks.alpha, isPlaying = true), awaitItem())
        }
    }

    @Test
    fun `commands without session are not sent`() = runTest {
        assertEquals(TransportOutcome.NoActiveSession, send(TransportCommand.TogglePlayPause))
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `commands with session reach controller`() = runTest {
        monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = false)

        assertEquals(TransportOutcome.Sent, send(TransportCommand.TogglePlayPause))
        send(TransportCommand.SkipToNext)
        send(TransportCommand.SkipToPrevious)

        assertEquals(
            listOf(PlaybackCommand.TogglePlayPause, PlaybackCommand.SkipToNext, PlaybackCommand.SkipToPrevious),
            controller.commands,
        )
    }
}
