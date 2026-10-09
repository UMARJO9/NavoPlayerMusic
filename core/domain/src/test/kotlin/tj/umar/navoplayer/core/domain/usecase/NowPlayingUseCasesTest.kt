package tj.umar.navoplayer.core.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.model.TransportOutcome
import tj.umar.navoplayer.core.testing.data.TestSavedQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakeNowPlayingMonitor
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

class NowPlayingUseCasesTest {

    private val monitor = FakeNowPlayingMonitor()
    private val controller = FakePlaybackController()
    private val queues = FakePlaybackQueueRepository()
    private val tracks = FakeTrackRepository()
    private val observe = ObserveNowPlayingUseCase(monitor, queues, tracks)
    private val send = SendTransportCommandUseCase(monitor, queues, controller)

    @Test
    fun `observe passes live session through`() = runTest {
        observe().test {
            assertEquals(NowPlaying.Idle, awaitItem())
            monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = true)
            assertEquals(NowPlaying(TestTracks.alpha, isPlaying = true), awaitItem())
        }
    }

    @Test
    fun `idle session falls back to saved track paused`() = runTest {
        tracks.emit(listOf(TestTracks.alpha, TestTracks.beta))
        queues.saveQueue(TestSavedQueues.alphaBeta)

        observe().test {
            assertEquals(NowPlaying(TestTracks.beta, isPlaying = false), awaitItem())
        }
    }

    @Test
    fun `missing saved track stays idle`() = runTest {
        tracks.emit(emptyList())
        queues.saveQueue(TestSavedQueues.alphaBeta)

        observe().test {
            assertEquals(NowPlaying.Idle, awaitItem())
        }
    }

    @Test
    fun `commands without session or saved queue are not sent`() = runTest {
        assertEquals(TransportOutcome.NoActiveSession, send(TransportCommand.TogglePlayPause))
        assertTrue(controller.commands.isEmpty())
    }

    @Test
    fun `play with saved queue resumes but skip does not`() = runTest {
        queues.saveQueue(TestSavedQueues.alphaBeta)

        assertEquals(TransportOutcome.Sent, send(TransportCommand.TogglePlayPause))
        assertEquals(TransportOutcome.NoActiveSession, send(TransportCommand.SkipToNext))
        assertEquals(listOf(PlaybackCommand.TogglePlayPause), controller.commands)
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
