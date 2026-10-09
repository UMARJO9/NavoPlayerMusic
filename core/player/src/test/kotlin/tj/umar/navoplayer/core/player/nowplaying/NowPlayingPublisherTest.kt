package tj.umar.navoplayer.core.player.nowplaying

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.testing.data.TestTracks

@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingPublisherTest {

    private val store = SessionNowPlayingStore()
    private val changes = MutableStateFlow(NowPlaying.Idle)
    private val publisher = NowPlayingPublisher(changes, store)

    @Test
    fun `store starts idle and follows changes`() = runTest {
        assertEquals(NowPlaying.Idle, store.observeNowPlaying().first())
        publisher.start(backgroundScope)
        changes.value = NowPlaying(TestTracks.alpha, isPlaying = true)
        runCurrent()

        assertEquals(NowPlaying(TestTracks.alpha, isPlaying = true), store.observeNowPlaying().first())
    }

    @Test
    fun `release resets to idle and stops following`() = runTest {
        publisher.start(backgroundScope)
        changes.value = NowPlaying(TestTracks.alpha, isPlaying = true)
        runCurrent()

        publisher.release()
        changes.value = NowPlaying(TestTracks.beta, isPlaying = true)
        runCurrent()

        assertEquals(NowPlaying.Idle, store.observeNowPlaying().first())
    }

    @Test
    fun `start after release follows again`() = runTest {
        publisher.start(backgroundScope)
        publisher.release()

        publisher.start(backgroundScope)
        changes.value = NowPlaying(TestTracks.beta, isPlaying = false)
        runCurrent()

        assertEquals(NowPlaying(TestTracks.beta, isPlaying = false), store.observeNowPlaying().first())
    }
}
