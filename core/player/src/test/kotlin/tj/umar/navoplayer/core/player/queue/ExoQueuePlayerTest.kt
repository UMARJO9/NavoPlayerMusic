package tj.umar.navoplayer.core.player.queue

import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.service.ShuffleOrderListener
import tj.umar.navoplayer.core.player.service.playOrder
import tj.umar.navoplayer.core.testing.data.TestTracks
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExoQueuePlayerTest {

    private val pending = PendingShuffleOrder()
    private val sourceStore = PlaybackSourceStore()
    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()
        .also { it.addListener(ShuffleOrderListener(it, Random(5), pending)) }
    private val queuePlayer = ExoQueuePlayer(player, sourceStore, pending)

    private val items = (1..5).map { TestTracks.alpha.copy(id = it.toLong()).toMediaItem("q$it") }

    @After
    fun tearDown() {
        player.release()
    }

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun restored(shuffleOrder: IntArray? = null) = RestoredMediaQueue(
        items = items,
        startIndex = 3,
        startPositionMs = 20_000,
        shuffleOrder = shuffleOrder,
        shuffleEnabled = shuffleOrder != null,
        repeatMode = Player.REPEAT_MODE_ONE,
        source = PlaybackSource.Playlist(2, "Mix"),
    )

    @Test
    fun `apply restores queue paused and idle`() {
        queuePlayer.apply(restored())
        idle()

        assertEquals(5, player.mediaItemCount)
        assertEquals(3, player.currentMediaItemIndex)
        assertEquals(20_000L, player.currentPosition)
        assertEquals(Player.REPEAT_MODE_ONE, player.repeatMode)
        assertEquals(Player.STATE_IDLE, player.playbackState)
        assertFalse(player.playWhenReady)
        assertEquals(PlaybackSource.Playlist(2, "Mix"), sourceStore.source.value)
        assertTrue(queuePlayer.hasCurrentItem)
    }

    @Test
    fun `apply restores exact shuffle order`() {
        queuePlayer.apply(restored(shuffleOrder = intArrayOf(3, 0, 4, 2, 1)))
        idle()

        assertTrue(player.shuffleModeEnabled)
        assertEquals(listOf(3, 0, 4, 2, 1), player.currentTimeline.playOrder(true).toList())
    }

    @Test
    fun `current resumption reports playlist and offers order`() {
        queuePlayer.apply(restored(shuffleOrder = intArrayOf(3, 0, 4, 2, 1)))
        idle()

        val resumption = queuePlayer.currentResumption()!!

        assertEquals(listOf("q1", "q2", "q3", "q4", "q5"), resumption.mediaItems.map { it.queueItemId() })
        assertEquals(3, resumption.startIndex)
        assertEquals(listOf(3, 0, 4, 2, 1), pending.take(player)?.toList())
    }

    @Test
    fun `current preview holds only current item without side effects`() {
        queuePlayer.apply(restored(shuffleOrder = intArrayOf(3, 0, 4, 2, 1)))
        idle()
        pending.take(player)

        val preview = queuePlayer.currentPreview()!!

        assertEquals(listOf("q4"), preview.mediaItems.map { it.queueItemId() })
        assertEquals(0, preview.startIndex)
        assertEquals(20_000L, preview.startPositionMs)
        assertEquals(null, pending.take(player))
    }

    @Test
    fun `empty player has no preview`() {
        assertEquals(null, queuePlayer.currentPreview())
    }
}
