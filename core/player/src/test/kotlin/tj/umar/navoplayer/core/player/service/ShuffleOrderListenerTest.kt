package tj.umar.navoplayer.core.player.service

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
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ShuffleOrderListenerTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()
        .also { it.addListener(ShuffleOrderListener(it, Random(7))) }

    private val items = (1..8).map { MediaItem.Builder().setMediaId("$it").setUri("content://media/$it").build() }

    @After
    fun tearDown() {
        player.release()
    }

    private fun firstShuffledIndex(): Int = player.currentTimeline.getFirstWindowIndex(true)

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    @Test
    fun `enabling shuffle starts order at current item`() {
        player.setMediaItems(items, 5, 0)
        idle()

        player.shuffleModeEnabled = true
        idle()

        assertEquals(5, firstShuffledIndex())
    }

    @Test
    fun `replaying same queue starts order at new start item`() {
        player.shuffleModeEnabled = true
        player.setMediaItems(items, 2, 0)
        idle()
        assertEquals(2, firstShuffledIndex())

        player.setMediaItems(items, 6, 0)
        idle()

        assertEquals(6, firstShuffledIndex())
    }

    private fun playedIds(): List<String> =
        player.currentTimeline.playOrder(true).map { player.getMediaItemAt(it).mediaId }

    private fun item(id: String) = MediaItem.Builder().setMediaId(id).setUri("content://media/$id").build()

    @Test
    fun `adding items keeps existing shuffled order`() {
        player.shuffleModeEnabled = true
        player.setMediaItems(items, 3, 0)
        idle()
        val before = playedIds()

        player.addMediaItems(4, listOf(item("next")))
        player.addMediaItem(item("last"))
        idle()

        val after = playedIds()
        assertEquals(before, after.filter { it != "next" && it != "last" })
        assertEquals("last", after.last())
    }

    @Test
    fun `removing item keeps remaining shuffled order`() {
        player.shuffleModeEnabled = true
        player.setMediaItems(items, 3, 0)
        idle()
        val before = playedIds()

        player.removeMediaItem(6)
        idle()

        assertEquals(before.filter { it != "7" }, playedIds())
    }
}
