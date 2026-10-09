package tj.umar.navoplayer.core.player.service

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.data.TestTracks

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

    @Test
    fun `queue commands are granted only to own package`() {
        val own = sessionCommandsFor("tj.umar.navoplayer", ownPackage = "tj.umar.navoplayer")
        val foreign = sessionCommandsFor("com.example.remote", ownPackage = "tj.umar.navoplayer")

        QueueSessionCommands.all.forEach { command ->
            assertTrue(own.contains(command))
            assertFalse(foreign.contains(command))
        }
    }

    @Test
    fun `queue command reaches editor`() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).setLooper(Looper.getMainLooper()).build()
        try {
            player.setMediaItems(listOf(TestTracks.alpha.toMediaItem("q1"), TestTracks.beta.toMediaItem("q2")))
            val callback = PlaybackSessionCallback("own", QueueEditor(player))
            val remove = QueueSessionCommands.encode(QueueRequest.Remove("q2"))

            assertEquals(SessionResult.RESULT_SUCCESS, callback.handleQueueCommand(remove.command, remove.args))
            assertEquals(1, player.mediaItemCount)
        } finally {
            player.release()
        }
    }

    @Test
    fun `rejected or malformed queue command reports bad value`() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).setLooper(Looper.getMainLooper()).build()
        try {
            val callback = PlaybackSessionCallback("own", QueueEditor(player))
            val remove = QueueSessionCommands.encode(QueueRequest.Remove("missing"))

            assertEquals(SessionError.ERROR_BAD_VALUE, callback.handleQueueCommand(remove.command, remove.args))
            assertEquals(SessionError.ERROR_BAD_VALUE, callback.handleQueueCommand(remove.command, Bundle()))
        } finally {
            player.release()
        }
    }
}
