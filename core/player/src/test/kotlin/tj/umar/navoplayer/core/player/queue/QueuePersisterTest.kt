package tj.umar.navoplayer.core.player.queue

import android.os.Looper
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.usecase.ClearSavedPlaybackQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackProgressUseCase
import tj.umar.navoplayer.core.domain.usecase.SavePlaybackQueueUseCase
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository

private class FakeQueuePlayer : QueuePlayer {
    var captured: CapturedQueue? = null
    override var hasCurrentItem: Boolean = true
    override var isPlaying: Boolean = false
    override val events = MutableSharedFlow<QueuePlayerEvent>(extraBufferCapacity = 16)
    override fun capture(): CapturedQueue? = captured
    override fun apply(queue: RestoredMediaQueue) = Unit
    override fun prepareForResumption(queue: RestoredMediaQueue) = Unit
    override fun currentResumption(): MediaSession.MediaItemsWithStartPosition? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueuePersisterTest {

    private val exo = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()
    private val player = FakeQueuePlayer()
    private val repository = FakePlaybackQueueRepository()

    @After
    fun tearDown() {
        exo.release()
    }

    private fun capture(position: Long = 5_000) {
        exo.setMediaItems((1..3).map { TestTracks.alpha.copy(id = it.toLong()).toMediaItem("q$it") }, 1, position)
        shadowOf(Looper.getMainLooper()).idle()
        player.captured = exo.captureQueue(null)
    }

    private fun TestScope.persister(): QueuePersister {
        val writer = QueueStateWriter(
            SavePlaybackQueueUseCase(repository),
            SavePlaybackProgressUseCase(repository),
            ClearSavedPlaybackQueueUseCase(repository),
            backgroundScope,
        )
        return QueuePersister(player, writer, StandardTestDispatcher(testScheduler))
    }

    @Test
    fun `structure changes are debounced into one full save`() = runTest {
        capture()
        persister().start(backgroundScope, saveNow = false)
        runCurrent()

        repeat(3) { player.events.emit(QueuePlayerEvent.StructureChanged) }
        advanceTimeBy(QUEUE_SAVE_DEBOUNCE_MILLIS + 1)
        runCurrent()

        assertEquals(1, repository.saveCalls)
        assertEquals(listOf(1L, 2L, 3L), repository.current?.items?.map { it.trackId })
    }

    @Test
    fun `progress change saves progress only`() = runTest {
        capture()
        val persister = persister()
        persister.start(backgroundScope, saveNow = true)
        advanceTimeBy(1)
        runCurrent()

        capture(position = 9_000)
        player.events.emit(QueuePlayerEvent.ProgressChanged)
        advanceTimeBy(QUEUE_SAVE_DEBOUNCE_MILLIS + 1)
        runCurrent()

        assertEquals(1, repository.saveCalls)
        assertEquals(1, repository.progressCalls)
        assertEquals(9_000L, repository.current?.progress?.positionMs)
    }

    @Test
    fun `pause flushes and ticker saves while playing`() = runTest {
        capture()
        persister().start(backgroundScope, saveNow = true)
        advanceTimeBy(1)
        runCurrent()

        player.events.emit(QueuePlayerEvent.PlayingChanged(true))
        advanceTimeBy(QUEUE_POSITION_TICK_MILLIS * 2 + 1)
        runCurrent()
        assertEquals(2, repository.progressCalls)

        player.events.emit(QueuePlayerEvent.PlayingChanged(false))
        runCurrent()
        assertEquals(3, repository.progressCalls)

        advanceTimeBy(QUEUE_POSITION_TICK_MILLIS * 2)
        runCurrent()
        assertEquals(3, repository.progressCalls)
    }

    @Test
    fun `emptied queue clears saved state`() = runTest {
        capture()
        persister().start(backgroundScope, saveNow = true)
        advanceTimeBy(1)
        runCurrent()

        player.events.emit(QueuePlayerEvent.Emptied)
        runCurrent()

        assertNull(repository.current)
    }

    @Test
    fun `flush on empty player writes nothing and release stops writes`() = runTest {
        val persister = persister()
        persister.start(backgroundScope, saveNow = false)
        runCurrent()

        persister.flush()
        persister.release()
        capture()
        player.events.emit(QueuePlayerEvent.StructureChanged)
        advanceTimeBy(QUEUE_SAVE_DEBOUNCE_MILLIS + 1)
        runCurrent()

        assertEquals(0, repository.saveCalls)
        assertEquals(0, repository.progressCalls)
    }
}
