package tj.umar.navoplayer.core.player.service

import android.net.Uri
import androidx.media3.common.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaybackSessionCallbackTest {

    private val contentUri = "content://media/external/audio/media/1"

    @Test
    fun `request uri becomes playback uri`() {
        val item = MediaItem.Builder()
            .setMediaId("1")
            .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(Uri.parse(contentUri)).build())
            .build()

        val resolved = resolvePlayableItems(listOf(item)).single()

        assertEquals(contentUri, resolved.localConfiguration?.uri.toString())
    }

    @Test
    fun `existing local uri is kept`() {
        val item = MediaItem.Builder().setMediaId("1").setUri(contentUri).build()

        assertEquals(contentUri, resolvePlayableItems(listOf(item)).single().localConfiguration?.uri.toString())
    }

    @Test
    fun `items without uri are dropped`() {
        assertTrue(resolvePlayableItems(listOf(MediaItem.Builder().setMediaId("1").build())).isEmpty())
    }

    @Test
    fun `non content uris are dropped`() {
        val remote = MediaItem.Builder().setMediaId("2").setUri("https://example.com/a.mp3").build()
        val file = MediaItem.Builder().setMediaId("3").setUri("file:///sdcard/a.mp3").build()

        assertTrue(resolvePlayableItems(listOf(remote, file)).isEmpty())
    }
}
