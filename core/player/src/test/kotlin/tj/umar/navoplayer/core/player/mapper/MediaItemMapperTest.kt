package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MediaItemMapperTest {

    @Test
    fun `full track survives round trip`() {
        assertEquals(TestTracks.alpha, TestTracks.alpha.toMediaItem().toTrack())
    }

    @Test
    fun `track with missing tags survives round trip`() {
        assertEquals(TestTracks.beta, TestTracks.beta.toMediaItem().toTrack())
    }

    @Test
    fun `media id and request uri come from track`() {
        val item = TestTracks.alpha.toMediaItem()

        assertEquals("1", item.mediaId)
        assertEquals(TestTracks.alpha.contentUri, item.requestMetadata.mediaUri.toString())
    }

    @Test
    fun `uri falls back to request metadata without local configuration`() {
        val stripped = TestTracks.alpha.toMediaItem().buildUpon().setUri(null as String?).build()

        assertEquals(TestTracks.alpha.contentUri, stripped.toTrack().contentUri)
    }

    @Test
    fun `unknown media id maps to zero`() {
        assertEquals(0L, MediaItem.Builder().setMediaId("x").build().toTrack().id)
    }
}
