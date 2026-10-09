package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.testing.data.TestTracks

class PlaybackQueueTest {

    private val first = QueueItem(QueueItemId("q1"), TestTracks.alpha)
    private val second = QueueItem(QueueItemId("q2"), TestTracks.beta)
    private val repeated = QueueItem(QueueItemId("q3"), TestTracks.alpha)

    @Test
    fun `current follows current index`() {
        assertEquals(second, PlaybackQueue(listOf(first, second), currentIndex = 1).current)
    }

    @Test
    fun `current is null when index is out of range`() {
        assertNull(PlaybackQueue(listOf(first), currentIndex = 3).current)
        assertNull(PlaybackQueue.Empty.current)
    }

    @Test
    fun `index of finds item by id`() {
        val queue = PlaybackQueue(listOf(first, second, repeated), currentIndex = 0)

        assertEquals(2, queue.indexOf(QueueItemId("q3")))
        assertEquals(-1, queue.indexOf(QueueItemId("missing")))
    }

    @Test
    fun `same track twice keeps distinct items`() {
        assertNotEquals(first, repeated)
        assertEquals(first.track, repeated.track)
    }
}
