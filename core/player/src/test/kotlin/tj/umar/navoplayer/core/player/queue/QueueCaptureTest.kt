package tj.umar.navoplayer.core.player.queue

import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.service.QueueShuffleOrder
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueueCaptureTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()

    @After
    fun tearDown() {
        player.release()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private val items = (1..4).map { TestTracks.alpha.copy(id = it.toLong()).toMediaItem("q$it") }

    @Test
    fun `empty player captures nothing`() {
        assertNull(player.captureQueue(null))
    }

    @Test
    fun `window order ids and progress are captured`() {
        player.setMediaItems(items, 2, 7_000)
        player.repeatMode = Player.REPEAT_MODE_ALL
        idle()

        val saved = player.captureQueue(PlaybackSource.Favorites)!!.toSavedQueue()!!

        assertEquals(listOf("q1", "q2", "q3", "q4"), saved.items.map { it.queueItemId.value })
        assertEquals(listOf(1L, 2L, 3L, 4L), saved.items.map { it.trackId })
        assertEquals(2, saved.progress.currentIndex)
        assertEquals(7_000L, saved.progress.positionMs)
        assertEquals(RepeatMode.All, saved.progress.repeatMode)
        assertEquals(PlaybackSource.Favorites, saved.source)
        assertNull(saved.shuffleOrder)
    }

    @Test
    fun `shuffle order is captured when shuffle is on`() {
        player.setMediaItems(items, 0, 0)
        player.setShuffleOrder(QueueShuffleOrder(intArrayOf(2, 0, 3, 1)))
        player.shuffleModeEnabled = true
        idle()

        val saved = player.captureQueue(null)!!.toSavedQueue()!!

        assertEquals(listOf(2, 0, 3, 1), saved.shuffleOrder)
        assertEquals(true, saved.progress.shuffleEnabled)
    }

    @Test
    fun `untagged items are skipped and current index adjusted`() {
        val foreign = MediaItem.Builder().setMediaId("x").setUri("content://media/x").build()
        player.setMediaItems(listOf(foreign) + items.take(2), 2, 0)
        idle()

        val saved = player.captureQueue(null)!!.toSavedQueue()!!

        assertEquals(listOf("q1", "q2"), saved.items.map { it.queueItemId.value })
        assertEquals(1, saved.progress.currentIndex)
    }
}
