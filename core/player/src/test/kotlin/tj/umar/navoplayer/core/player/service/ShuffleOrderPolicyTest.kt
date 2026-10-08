package tj.umar.navoplayer.core.player.service

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class ShuffleOrderPolicyTest {

    @Test
    fun `start index comes first`() {
        val order = shuffledIndicesStartingAt(length = 10, startIndex = 7, random = Random(1))

        assertEquals(7, order.first())
    }

    @Test
    fun `order is a full permutation`() {
        val order = shuffledIndicesStartingAt(length = 50, startIndex = 3, random = Random(2))

        assertEquals((0 until 50).toList(), order.sorted())
    }

    @Test
    fun `empty and single item queues`() {
        assertArrayEquals(IntArray(0), shuffledIndicesStartingAt(0, 0, Random(3)))
        assertArrayEquals(intArrayOf(0), shuffledIndicesStartingAt(1, 0, Random(3)))
    }

    @Test
    fun `out of range start is clamped`() {
        assertEquals(4, shuffledIndicesStartingAt(5, 99, Random(4)).first())
    }

    @Test
    fun `same seed gives same order`() {
        assertArrayEquals(
            shuffledIndicesStartingAt(20, 5, Random(9)),
            shuffledIndicesStartingAt(20, 5, Random(9)),
        )
    }
}
