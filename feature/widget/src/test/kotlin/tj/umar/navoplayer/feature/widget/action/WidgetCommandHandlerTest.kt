package tj.umar.navoplayer.feature.widget.action

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.usecase.SendTransportCommandUseCase
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakeNowPlayingMonitor
import tj.umar.navoplayer.core.testing.playback.FakePlaybackController
import tj.umar.navoplayer.core.testing.playback.PlaybackCommand
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository

class WidgetCommandHandlerTest {

    private val monitor = FakeNowPlayingMonitor()
    private val controller = FakePlaybackController()
    private var refreshCalls = 0
    private val handler = WidgetCommandHandler(SendTransportCommandUseCase(monitor, FakePlaybackQueueRepository(), controller)) { refreshCalls++ }

    @Test
    fun `active session sends command without refresh`() = runTest {
        monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = true)

        handler.handle(TransportCommand.SkipToNext)

        assertEquals(listOf(PlaybackCommand.SkipToNext), controller.commands)
        assertEquals(0, refreshCalls)
    }

    @Test
    fun `no session refreshes widget instead`() = runTest {
        handler.handle(TransportCommand.TogglePlayPause)

        assertTrue(controller.commands.isEmpty())
        assertEquals(1, refreshCalls)
    }
}
