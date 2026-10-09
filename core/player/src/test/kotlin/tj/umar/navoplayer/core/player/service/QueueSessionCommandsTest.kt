package tj.umar.navoplayer.core.player.service

import android.os.Bundle
import androidx.media3.session.SessionCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.mapper.toTrack
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QueueSessionCommandsTest {

    private fun roundTrip(request: QueueRequest): QueueRequest? {
        val encoded = QueueSessionCommands.encode(request)
        return QueueSessionCommands.decode(encoded.command, encoded.args)
    }

    @Test
    fun `enqueue survives round trip`() {
        val request = QueueRequest.Enqueue(listOf(TestTracks.alpha.toMediaItem("q1")), QueueInsertion.Next)

        val decoded = roundTrip(request) as QueueRequest.Enqueue

        assertEquals(QueueInsertion.Next, decoded.insertion)
        assertEquals("q1", decoded.items.single().queueItemId())
        assertEquals(TestTracks.alpha, decoded.items.single().toTrack())
    }

    @Test
    fun `remove and move survive round trip`() {
        assertEquals(QueueRequest.Remove("q2"), roundTrip(QueueRequest.Remove("q2")))
        assertEquals(QueueRequest.Move("q3", 4), roundTrip(QueueRequest.Move("q3", 4)))
    }

    @Test
    fun `malformed args decode to null`() {
        val enqueue = QueueSessionCommands.encode(QueueRequest.Remove("q")).command
        val move = QueueSessionCommands.encode(QueueRequest.Move("q", 1)).command

        assertNull(QueueSessionCommands.decode(enqueue, Bundle()))
        assertNull(QueueSessionCommands.decode(move, Bundle().apply { putString("queue_item_id", "q") }))
        assertNull(QueueSessionCommands.decode(SessionCommand("other", Bundle.EMPTY), Bundle()))
    }

    @Test
    fun `enqueue without items decodes to null`() {
        val encoded = QueueSessionCommands.encode(QueueRequest.Enqueue(emptyList(), QueueInsertion.Last))

        assertNull(QueueSessionCommands.decode(encoded.command, encoded.args))
    }
}
