package tj.umar.navoplayer.core.player.queue

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.usecase.LoadResumableQueueUseCase
import tj.umar.navoplayer.core.player.artwork.ResumptionArtwork
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.app.FakeAudioAccessChecker
import tj.umar.navoplayer.core.testing.data.TestSavedQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.repository.FakePlaybackQueueRepository
import tj.umar.navoplayer.core.testing.repository.FakeTrackRepository

private class RecordingQueuePlayer : QueuePlayer {
    val applied = mutableListOf<RestoredMediaQueue>()
    val prepared = mutableListOf<RestoredMediaQueue>()
    var current: MediaSession.MediaItemsWithStartPosition? = null
    var preview: MediaSession.MediaItemsWithStartPosition? = null
    override var hasCurrentItem: Boolean = false
    override val isPlaying: Boolean = false
    override val events = MutableSharedFlow<QueuePlayerEvent>()
    override fun capture(): CapturedQueue? = null
    override fun apply(queue: RestoredMediaQueue) {
        applied += queue
        hasCurrentItem = true
    }
    override fun prepareForResumption(queue: RestoredMediaQueue) {
        prepared += queue
    }
    override fun currentResumption(): MediaSession.MediaItemsWithStartPosition? = current
    override fun currentPreview(): MediaSession.MediaItemsWithStartPosition? = preview
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueueRestorerTest {

    private val queues = FakePlaybackQueueRepository(TestSavedQueues.alphaBeta)
    private val tracks = FakeTrackRepository()
    private val access = FakeAudioAccessChecker()
    private val player = RecordingQueuePlayer()
    private val renderedIds = mutableListOf<Long>()
    private val artwork = ResumptionArtwork { id ->
        renderedIds += id
        byteArrayOf(id.toByte())
    }

    private suspend fun TestScope.restorer(): QueueRestorer {
        tracks.emit(listOf(TestTracks.alpha, TestTracks.beta))
        return QueueRestorer(
            LoadResumableQueueUseCase(queues, tracks, access),
            player,
            StandardTestDispatcher(testScheduler),
            artwork,
        )
    }

    @Test
    fun `saved queue is applied to empty player`() = runTest {
        val restorer = restorer()

        restorer.start(backgroundScope)
        runCurrent()

        val applied = player.applied.single()
        assertEquals(listOf("q1", "q2"), applied.items.map { it.queueItemId() })
        assertEquals(1, applied.startIndex)
        assertEquals(12_000L, applied.startPositionMs)
        assertTrue(restorer.settled.value)
    }

    @Test
    fun `busy player is left alone but restore settles`() = runTest {
        player.hasCurrentItem = true
        val restorer = restorer()

        restorer.start(backgroundScope)
        runCurrent()

        assertTrue(player.applied.isEmpty())
        assertTrue(restorer.settled.value)
    }

    @Test
    fun `denied access settles without applying`() = runTest {
        access.granted = false
        val restorer = restorer()

        restorer.start(backgroundScope)
        runCurrent()

        assertTrue(player.applied.isEmpty())
        assertTrue(restorer.settled.value)
    }

    @Test
    fun `resumption before start finishes claims queue`() = runTest {
        val restorer = restorer()

        val future = restorer.resumption(backgroundScope)
        restorer.start(backgroundScope)
        runCurrent()

        val result = future.await()
        assertEquals(2, result.mediaItems.size)
        assertEquals(1, result.startIndex)
        assertEquals(1, player.prepared.size)
        assertTrue(player.applied.isEmpty())
    }

    @Test
    fun `resumption after restore returns current playlist`() = runTest {
        val restorer = restorer()
        restorer.start(backgroundScope)
        runCurrent()
        val current = MediaSession.MediaItemsWithStartPosition(listOf(MediaItem.EMPTY), 0, 0)
        player.current = current

        assertEquals(current, restorer.resumption(backgroundScope).await())
    }

    @Test
    fun `resumption without saved queue fails`() = runTest {
        val restorer = QueueRestorer(
            LoadResumableQueueUseCase(FakePlaybackQueueRepository(), tracks, access),
            player,
            StandardTestDispatcher(testScheduler),
        )

        val failure = runCatching { restorer.resumption(backgroundScope).await() }.exceptionOrNull()

        assertTrue(failure is UnsupportedOperationException)
    }

    @Test
    fun `resumption after queue was emptied reloads saved state`() = runTest {
        val restorer = restorer()
        restorer.start(backgroundScope)
        runCurrent()
        player.hasCurrentItem = false
        queues.clearQueue()

        val failure = runCatching { restorer.resumption(backgroundScope).await() }.exceptionOrNull()

        assertTrue(failure is UnsupportedOperationException)
    }

    @Test
    fun `access granted later is picked up by resumption`() = runTest {
        access.granted = false
        val restorer = restorer()
        restorer.start(backgroundScope)
        runCurrent()

        access.granted = true
        val result = restorer.resumption(backgroundScope).await()

        assertEquals(2, result.mediaItems.size)
    }

    @Test
    fun `preview before restore shows start item without claiming queue`() = runTest {
        val restorer = restorer()

        val result = restorer.preview(backgroundScope).await()

        val item = result.mediaItems.single()
        assertEquals(TestTracks.beta.id.toString(), item.mediaId)
        assertEquals(0, result.startIndex)
        assertEquals(12_000L, result.startPositionMs)
        assertEquals(true, item.mediaMetadata.isPlayable)
        assertEquals(listOf(TestTracks.beta.id), renderedIds)
        assertArrayEquals(byteArrayOf(TestTracks.beta.id.toByte()), item.mediaMetadata.artworkData)
        assertTrue(player.prepared.isEmpty())
        assertTrue(player.applied.isEmpty())
    }

    @Test
    fun `preview does not prevent start from applying queue`() = runTest {
        val restorer = restorer()

        restorer.preview(backgroundScope)
        restorer.start(backgroundScope)
        runCurrent()

        assertEquals(1, player.applied.size)
        assertTrue(restorer.settled.value)
    }

    @Test
    fun `preview after restore uses current player item`() = runTest {
        val restorer = restorer()
        restorer.start(backgroundScope)
        runCurrent()
        player.preview = MediaSession.MediaItemsWithStartPosition(listOf(TestTracks.alpha.toMediaItem("q9")), 0, 3_000)

        val result = restorer.preview(backgroundScope).await()

        assertEquals(TestTracks.alpha.id.toString(), result.mediaItems.single().mediaId)
        assertEquals(3_000L, result.startPositionMs)
        assertEquals(listOf(TestTracks.alpha.id), renderedIds)
    }

    @Test
    fun `preview without saved queue fails`() = runTest {
        val restorer = QueueRestorer(
            LoadResumableQueueUseCase(FakePlaybackQueueRepository(), tracks, access),
            player,
            StandardTestDispatcher(testScheduler),
            artwork,
        )

        val failure = runCatching { restorer.preview(backgroundScope).await() }.exceptionOrNull()

        assertTrue(failure is UnsupportedOperationException)
    }

    @Test
    fun `preview with denied access fails`() = runTest {
        access.granted = false
        val restorer = restorer()

        val failure = runCatching { restorer.preview(backgroundScope).await() }.exceptionOrNull()

        assertTrue(failure is UnsupportedOperationException)
    }

    @Test
    fun `failing artwork still yields preview without picture`() = runTest {
        tracks.emit(listOf(TestTracks.alpha, TestTracks.beta))
        val restorer = QueueRestorer(
            LoadResumableQueueUseCase(queues, tracks, access),
            player,
            StandardTestDispatcher(testScheduler),
            ResumptionArtwork { error("render failed") },
        )

        val item = restorer.preview(backgroundScope).await().mediaItems.single()

        assertEquals(TestTracks.beta.id.toString(), item.mediaId)
        assertNull(item.mediaMetadata.artworkData)
    }

    @Test
    fun `cancelled preview does not render artwork`() = runTest {
        val restorer = restorer()

        restorer.preview(backgroundScope).cancel(false)
        runCurrent()

        assertTrue(renderedIds.isEmpty())
    }

    @Test
    fun `resumption after preview returns full queue`() = runTest {
        val restorer = restorer()
        restorer.preview(backgroundScope).await()

        val result = restorer.resumption(backgroundScope).await()

        assertEquals(2, result.mediaItems.size)
        assertEquals(1, result.startIndex)
        assertEquals(1, player.prepared.size)
    }
}
