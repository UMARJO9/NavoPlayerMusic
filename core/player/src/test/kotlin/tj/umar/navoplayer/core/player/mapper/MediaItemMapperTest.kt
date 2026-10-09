package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MediaItemMapperTest {

    @Test
    fun `full track survives round trip`() {
        assertEquals(TestTracks.alpha, TestTracks.alpha.toMediaItem("q1").toTrack())
    }

    @Test
    fun `track with missing tags survives round trip`() {
        assertEquals(TestTracks.beta, TestTracks.beta.toMediaItem("q1").toTrack())
    }

    @Test
    fun `media id and request uri come from track`() {
        val item = TestTracks.alpha.toMediaItem("q1")

        assertEquals("1", item.mediaId)
        assertEquals(TestTracks.alpha.contentUri, item.requestMetadata.mediaUri.toString())
    }

    @Test
    fun `uri falls back to request metadata without local configuration`() {
        val stripped = TestTracks.alpha.toMediaItem("q1").buildUpon().setUri(null as String?).build()

        assertEquals(TestTracks.alpha.contentUri, stripped.toTrack().contentUri)
    }

    @Test
    fun `queue item id survives round trip`() {
        val item = TestTracks.alpha.toMediaItem("q7")

        assertEquals("q7", item.queueItemId())
        assertEquals(QueueItem(QueueItemId("q7"), TestTracks.alpha), item.toQueueItem(fallbackKey = 3))
    }

    @Test
    fun `missing queue item id falls back to media id and key`() {
        val item = MediaItem.Builder().setMediaId("5").build()

        assertNull(item.queueItemId())
        assertEquals(QueueItemId("5@3"), item.toQueueItem(fallbackKey = 3).id)
    }

    @Test
    fun `unknown media id maps to zero`() {
        assertEquals(0L, MediaItem.Builder().setMediaId("x").build().toTrack().id)
    }

    @Test
    fun `missing queue item id is assigned once`() {
        val untagged = MediaItem.Builder().setMediaId("5").build()

        assertEquals("new", untagged.withQueueItemId { "new" }.queueItemId())
        assertEquals("q7", TestTracks.alpha.toMediaItem("q7").withQueueItemId { "new" }.queueItemId())
    }
}
