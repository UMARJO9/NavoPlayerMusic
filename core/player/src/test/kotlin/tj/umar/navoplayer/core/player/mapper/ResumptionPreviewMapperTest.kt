package tj.umar.navoplayer.core.player.mapper

import androidx.media3.common.MediaMetadata
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ResumptionPreviewMapperTest {

    private val item = TestTracks.alpha.toMediaItem("q1")

    @Test
    fun `preview is playable music and keeps track data`() {
        val preview = item.toResumptionPreview(null)

        assertEquals(true, preview.mediaMetadata.isPlayable)
        assertEquals(false, preview.mediaMetadata.isBrowsable)
        assertEquals(MediaMetadata.MEDIA_TYPE_MUSIC, preview.mediaMetadata.mediaType)
        assertEquals(item.mediaId, preview.mediaId)
        assertEquals(TestTracks.alpha, preview.toTrack())
        assertEquals("q1", preview.queueItemId())
        assertNull(preview.mediaMetadata.artworkData)
    }

    @Test
    fun `artwork bytes become front cover`() {
        val png = byteArrayOf(1, 2, 3)

        val preview = item.toResumptionPreview(png)

        assertArrayEquals(png, preview.mediaMetadata.artworkData)
        assertEquals(MediaMetadata.PICTURE_TYPE_FRONT_COVER, preview.mediaMetadata.artworkDataType)
    }
}
