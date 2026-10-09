package tj.umar.navoplayer.core.player.service

import android.os.Looper
import androidx.media3.common.MediaItem
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
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.data.TestTracks
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueueEditorTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()
        .also { it.addListener(ShuffleOrderListener(it, Random(3))) }

    private val editor = QueueEditor(player)

    @After
    fun tearDown() {
        player.release()
    }

    private fun item(id: String): MediaItem = TestTracks.alpha.copy(id = id.hashCode().toLong()).toMediaItem(id)

    private fun idle() = shadowOf(Looper.getMainLooper()).idle()

    private fun load(count: Int, start: Int) {
        player.setMediaItems((1..count).map { item("q$it") }, start, 0)
        idle()
    }

    private fun ids(): List<String?> = (0 until player.mediaItemCount).map { player.getMediaItemAt(it).queueItemId() }

    private fun playedIds(): List<String?> =
        player.currentTimeline.playOrder(player.shuffleModeEnabled).map { player.getMediaItemAt(it).queueItemId() }

    @Test
    fun `play next inserts after current item`() {
        load(count = 4, start = 1)

        assertTrue(editor.enqueue(listOf(item("a")), QueueInsertion.Next))
        idle()

        assertEquals(listOf("q1", "q2", "a", "q3", "q4"), ids())
    }

    @Test
    fun `repeated play next puts latest first`() {
        load(count = 3, start = 0)

        editor.enqueue(listOf(item("a")), QueueInsertion.Next)
        editor.enqueue(listOf(item("b")), QueueInsertion.Next)
        idle()

        assertEquals(listOf("q1", "b", "a", "q2", "q3"), ids())
    }

    @Test
    fun `add to queue appends`() {
        load(count = 3, start = 0)

        editor.enqueue(listOf(item("a"), item("b")), QueueInsertion.Last)
        idle()

        assertEquals(listOf("q1", "q2", "q3", "a", "b"), ids())
    }

    @Test
    fun `shuffled play next plays right after current`() {
        player.shuffleModeEnabled = true
        load(count = 6, start = 2)
        val current = playedIds().first()

        editor.enqueue(listOf(item("a")), QueueInsertion.Next)
        idle()

        assertEquals(listOf(current, "a"), playedIds().take(2))
    }

    @Test
    fun `shuffled add to queue plays last`() {
        player.shuffleModeEnabled = true
        load(count = 6, start = 2)
        val before = playedIds()

        editor.enqueue(listOf(item("a")), QueueInsertion.Last)
        idle()

        assertEquals(before + "a", playedIds())
    }

    @Test
    fun `enqueue into empty player starts playback`() {
        editor.enqueue(listOf(item("a")), QueueInsertion.Next)
        idle()

        assertEquals(listOf("a"), ids())
        assertTrue(player.playWhenReady)
    }

    @Test
    fun `paused player stays paused after enqueue`() {
        load(count = 2, start = 0)

        editor.enqueue(listOf(item("a")), QueueInsertion.Last)
        idle()

        assertFalse(player.playWhenReady)
    }

    @Test
    fun `remove drops other item`() {
        load(count = 3, start = 0)

        assertTrue(editor.remove("q3"))
        idle()

        assertEquals(listOf("q1", "q2"), ids())
    }

    @Test
    fun `current and unknown items are not removed`() {
        load(count = 3, start = 1)

        assertFalse(editor.remove("q2"))
        assertFalse(editor.remove("missing"))
        idle()

        assertEquals(listOf("q1", "q2", "q3"), ids())
    }

    @Test
    fun `move reorders playlist without shuffle`() {
        load(count = 4, start = 0)

        assertTrue(editor.move("q4", 1))
        idle()

        assertEquals(listOf("q1", "q4", "q2", "q3"), ids())
    }

    @Test
    fun `shuffled move changes play order only`() {
        player.shuffleModeEnabled = true
        load(count = 5, start = 0)
        val playlist = ids()
        val before = playedIds()
        val moved = before.last()!!

        assertTrue(editor.move(moved, 1))
        idle()

        assertEquals(playlist, ids())
        assertEquals(before.first(), playedIds().first())
        assertEquals(moved, playedIds()[1])
        assertEquals(0, player.currentMediaItemIndex)
    }

    @Test
    fun `unknown item is not moved`() {
        load(count = 2, start = 0)

        assertFalse(editor.move("missing", 0))
    }

}
