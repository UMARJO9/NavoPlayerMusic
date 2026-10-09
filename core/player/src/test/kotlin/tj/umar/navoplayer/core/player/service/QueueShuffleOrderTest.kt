package tj.umar.navoplayer.core.player.service

import androidx.media3.common.C
import androidx.media3.exoplayer.source.ShuffleOrder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueShuffleOrderTest {

    private val order = QueueShuffleOrder(intArrayOf(2, 0, 3, 1))

    private fun ShuffleOrder.sequence(): IntArray = (this as QueueShuffleOrder).playOrder

    @Test
    fun `navigation follows play order`() {
        assertEquals(4, order.length)
        assertEquals(2, order.firstIndex)
        assertEquals(1, order.lastIndex)
        assertEquals(3, order.getNextIndex(0))
        assertEquals(C.INDEX_UNSET, order.getNextIndex(1))
        assertEquals(2, order.getPreviousIndex(0))
        assertEquals(C.INDEX_UNSET, order.getPreviousIndex(2))
    }

    @Test
    fun `empty order has no first or last`() {
        val empty = QueueShuffleOrder(IntArray(0))

        assertEquals(C.INDEX_UNSET, empty.firstIndex)
        assertEquals(C.INDEX_UNSET, empty.lastIndex)
    }

    @Test
    fun `insert in middle plays right after predecessor`() {
        val inserted = order.cloneAndInsert(1, 2)

        assertArrayEquals(intArrayOf(4, 0, 1, 2, 5, 3), inserted.sequence())
    }

    @Test
    fun `append plays last`() {
        val inserted = order.cloneAndInsert(4, 2)

        assertArrayEquals(intArrayOf(2, 0, 3, 1, 4, 5), inserted.sequence())
    }

    @Test
    fun `insert at start plays first`() {
        val inserted = order.cloneAndInsert(0, 1)

        assertArrayEquals(intArrayOf(0, 3, 1, 4, 2), inserted.sequence())
    }

    @Test
    fun `remove keeps relative order`() {
        val removed = order.cloneAndRemove(1, 3)

        assertArrayEquals(intArrayOf(0, 1), removed.sequence())
    }

    @Test
    fun `removing everything drops queue order`() {
        val removed = order.cloneAndRemove(0, 4)

        assertFalse(removed is QueueShuffleOrder)
        assertEquals(0, removed.length)
    }

    @Test
    fun `move keeps every item at its play position`() {
        val moved = order.cloneAndMove(0, 1, 3)

        assertArrayEquals(intArrayOf(1, 3, 2, 0), moved.sequence())
    }

    @Test
    fun `clear drops queue order`() {
        val cleared = order.cloneAndClear()

        assertTrue(cleared !is QueueShuffleOrder)
        assertEquals(0, cleared.length)
    }
}
