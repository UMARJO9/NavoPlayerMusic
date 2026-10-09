package tj.umar.navoplayer.core.player.service

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class QueueOrdersTest {

    private val order = intArrayOf(3, 0, 4, 1, 2)

    @Test
    fun `item moves forward`() {
        assertArrayEquals(intArrayOf(0, 4, 3, 1, 2), order.withItemMoved(0, 2))
    }

    @Test
    fun `item moves back`() {
        assertArrayEquals(intArrayOf(2, 3, 0, 4, 1), order.withItemMoved(4, 0))
    }

    @Test
    fun `same position keeps order`() {
        assertArrayEquals(order, order.withItemMoved(2, 2))
    }

    @Test
    fun `target beyond end moves item last`() {
        assertArrayEquals(intArrayOf(0, 4, 1, 2, 3), order.withItemMoved(0, 99))
    }

    @Test
    fun `inserted items follow anchor`() {
        assertArrayEquals(intArrayOf(3, 0, 7, 8, 4, 1, 2), order.insertedAfter(1, intArrayOf(7, 8)))
    }

    @Test
    fun `negative anchor inserts at start and last anchor appends`() {
        assertArrayEquals(intArrayOf(9, 3, 0, 4, 1, 2), order.insertedAfter(-1, intArrayOf(9)))
        assertArrayEquals(intArrayOf(3, 0, 4, 1, 2, 9), order.insertedAfter(4, intArrayOf(9)))
    }
}
