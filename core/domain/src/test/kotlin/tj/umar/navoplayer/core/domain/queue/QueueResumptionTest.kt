package tj.umar.navoplayer.core.domain.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueItem
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.testing.data.TestTracks

class QueueResumptionTest {

    private val tracks = (1L..4L).map { TestTracks.alpha.copy(id = it, durationMs = 100_000) }
    private val all = tracks.associateBy { it.id }

    private fun saved(
        current: Int = 1,
        position: Long = 30_000,
        shuffleOrder: List<Int>? = null,
        trackIds: List<Long> = listOf(1, 2, 3, 4),
    ) = SavedQueue(
        items = trackIds.mapIndexed { index, id -> SavedQueueItem(QueueItemId("q$index"), id) },
        shuffleOrder = shuffleOrder,
        progress = SavedQueueProgress(current, position, shuffleOrder != null, RepeatMode.All),
        source = null,
    )

    private fun without(vararg ids: Long): Map<Long, Track> = all - ids.toSet()

    @Test
    fun `everything present keeps queue`() {
        val resumed = saved().resolveAgainst(all)!!

        assertEquals(listOf("q0", "q1", "q2", "q3"), resumed.items.map { it.id.value })
        assertEquals(1, resumed.startIndex)
        assertEquals(30_000L, resumed.startPositionMs)
        assertEquals(RepeatMode.All, resumed.repeatMode)
    }

    @Test
    fun `missing other item shifts indices and keeps current`() {
        val resumed = saved(current = 2).resolveAgainst(without(1))!!

        assertEquals(listOf("q1", "q2", "q3"), resumed.items.map { it.id.value })
        assertEquals(1, resumed.startIndex)
        assertEquals(30_000L, resumed.startPositionMs)
    }

    @Test
    fun `missing current moves to next in window order`() {
        val resumed = saved(current = 1).resolveAgainst(without(2))!!

        assertEquals("q2", resumed.items[resumed.startIndex].id.value)
        assertEquals(0L, resumed.startPositionMs)
    }

    @Test
    fun `missing current moves to next in shuffle order`() {
        val resumed = saved(current = 1, shuffleOrder = listOf(2, 1, 3, 0)).resolveAgainst(without(2))!!

        assertEquals("q3", resumed.items[resumed.startIndex].id.value)
        assertEquals(listOf(1, 2, 0), resumed.shuffleOrder)
    }

    @Test
    fun `missing last in play order moves to previous`() {
        val resumed = saved(current = 3).resolveAgainst(without(4))!!

        assertEquals("q2", resumed.items[resumed.startIndex].id.value)
    }

    @Test
    fun `all missing gives nothing`() {
        assertNull(saved().resolveAgainst(emptyMap()))
    }

    @Test
    fun `duplicate tracks are kept`() {
        val resumed = saved(trackIds = listOf(1, 1, 2)).resolveAgainst(all)!!

        assertEquals(3, resumed.items.size)
    }

    @Test
    fun `invalid shuffle order is dropped`() {
        assertNull(saved(shuffleOrder = listOf(0, 0, 1, 2)).resolveAgainst(all)!!.shuffleOrder)
    }

    @Test
    fun `position past end restarts track`() {
        assertEquals(0L, saved(position = 200_000).resolveAgainst(all)!!.startPositionMs)
    }

    @Test
    fun `out of range current starts at first`() {
        assertEquals(0, saved(current = 9).resolveAgainst(all)!!.startIndex)
    }
}
