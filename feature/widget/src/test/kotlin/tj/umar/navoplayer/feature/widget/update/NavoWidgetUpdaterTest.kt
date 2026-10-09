package tj.umar.navoplayer.feature.widget.update

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.usecase.ObserveNowPlayingUseCase
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.playback.FakeNowPlayingMonitor
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

@OptIn(ExperimentalCoroutinesApi::class)
class NavoWidgetUpdaterTest {

    private val monitor = FakeNowPlayingMonitor()
    private var refreshCalls = 0
    private var refreshError: Exception? = null
    private val refresher = WidgetRefresher {
        refreshCalls++
        refreshError?.let { throw it }
    }

    private fun TestScope.updater() = NavoWidgetUpdater(ObserveNowPlayingUseCase(monitor, FakePlaybackQueueRepository(), FakeTrackRepository()), refresher, backgroundScope)

    @Test
    fun `refreshes on start and on changes`() = runTest {
        updater().start()
        runCurrent()
        assertEquals(1, refreshCalls)

        monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = true)
        runCurrent()
        monitor.nowPlaying.value = NowPlaying(TestTracks.alpha, isPlaying = false)
        runCurrent()

        assertEquals(3, refreshCalls)
    }

    @Test
    fun `same widget state does not refresh again`() = runTest {
        updater().start()
        runCurrent()

        monitor.nowPlaying.value = NowPlaying.Idle.copy()
        runCurrent()

        assertEquals(1, refreshCalls)
    }

    @Test
    fun `second start does not collect twice`() = runTest {
        val updater = updater()
        updater.start()
        updater.start()
        runCurrent()

        monitor.nowPlaying.value = NowPlaying(TestTracks.beta, isPlaying = true)
        runCurrent()

        assertEquals(2, refreshCalls)
    }

    @Test
    fun `failing refresh does not stop updates`() = runTest {
        refreshError = IllegalStateException("launcher")
        updater().start()
        runCurrent()
        refreshError = null

        monitor.nowPlaying.value = NowPlaying(TestTracks.beta, isPlaying = true)
        runCurrent()

        assertEquals(2, refreshCalls)
    }
}
