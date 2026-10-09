package tj.umar.navoplayer.core.player.controller

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.service.QueueShuffleOrder
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueueMapperTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()

    @After
    fun tearDown() {
        player.release()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun load(items: List<MediaItem>, start: Int) {
        player.setMediaItems(items, start, 0)
        idle()
    }

    private val tracks = listOf(TestTracks.alpha, TestTracks.beta, TestTracks.alpha)

    @Test
    fun `unshuffled queue follows playlist`() {
        load(tracks.mapIndexed { index, track -> track.toMediaItem("q$index") }, start = 1)

        val queue = player.queueSnapshot().toPlaybackQueue()

        assertEquals(listOf("q0", "q1", "q2"), queue.items.map { it.id.value })
        assertEquals(tracks, queue.items.map { it.track })
        assertEquals(1, queue.currentIndex)
    }

    @Test
    fun `shuffled queue follows play order`() {
        load(tracks.mapIndexed { index, track -> track.toMediaItem("q$index") }, start = 1)
        player.setShuffleOrder(QueueShuffleOrder(intArrayOf(2, 0, 1)))
        player.shuffleModeEnabled = true
        idle()

        val queue = player.queueSnapshot().toPlaybackQueue()

        assertEquals(listOf("q2", "q0", "q1"), queue.items.map { it.id.value })
        assertEquals(2, queue.currentIndex)
    }

    @Test
    fun `items without queue id get positional ids`() {
        val untagged = MediaItem.Builder().setMediaId("7").setUri("content://media/7").build()
        load(listOf(untagged, untagged), start = 0)

        val queue = player.queueSnapshot().toPlaybackQueue()

        assertEquals(listOf(QueueItemId("7@0"), QueueItemId("7@1")), queue.items.map { it.id })
    }

    @Test
    fun `empty player maps to empty queue`() {
        assertEquals(PlaybackQueue.Empty, player.queueSnapshot().toPlaybackQueue())
    }
}
