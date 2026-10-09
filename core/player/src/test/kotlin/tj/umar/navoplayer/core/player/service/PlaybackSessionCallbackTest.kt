package tj.umar.navoplayer.core.player.service

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
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

    private val libraryCommands = listOf(
        SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT,
        SessionCommand.COMMAND_CODE_LIBRARY_SUBSCRIBE,
        SessionCommand.COMMAND_CODE_LIBRARY_UNSUBSCRIBE,
        SessionCommand.COMMAND_CODE_LIBRARY_GET_CHILDREN,
    )

    private fun controller(packageName: String) = MediaSession.ControllerInfo.createTestOnlyControllerInfo(
        packageName,
        0,
        0,
        0,
        0,
        true,
        Bundle.EMPTY,
        true,
    )

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
        val own = sessionCommandsFor("tj.umar.navoplayer", isTrusted = true, ownPackage = "tj.umar.navoplayer")
        val foreign = sessionCommandsFor("com.example.remote", isTrusted = true, ownPackage = "tj.umar.navoplayer")
        val systemUi = sessionCommandsFor("com.android.systemui", isTrusted = true, ownPackage = "tj.umar.navoplayer")

        QueueSessionCommands.all.forEach { command ->
            assertTrue(own.contains(command))
            assertFalse(foreign.contains(command))
            assertFalse(systemUi.contains(command))
        }
    }

    @Test
    fun `library commands are granted only to trusted system ui`() {
        val systemUi = sessionCommandsFor("com.android.systemui", isTrusted = true, ownPackage = "own")
        val spoofed = sessionCommandsFor("com.android.systemui", isTrusted = false, ownPackage = "own")
        val foreign = sessionCommandsFor("com.example.remote", isTrusted = true, ownPackage = "own")
        val own = sessionCommandsFor("own", isTrusted = true, ownPackage = "own")

        libraryCommands.forEach { code ->
            assertTrue(systemUi.contains(code))
            assertFalse(spoofed.contains(code))
            assertFalse(foreign.contains(code))
            assertFalse(own.contains(code))
        }
        assertFalse(systemUi.contains(SessionCommand.COMMAND_CODE_LIBRARY_SEARCH))
        assertFalse(systemUi.contains(SessionCommand.COMMAND_CODE_LIBRARY_GET_ITEM))
    }

    @Test
    fun `library root is refused`() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).setLooper(Looper.getMainLooper()).build()
        val callback = PlaybackSessionCallback("own", QueueEditor(player))
        val session = MediaLibraryService.MediaLibrarySession.Builder(RuntimeEnvironment.getApplication(), player, callback).build()
        try {
            val result = callback.onGetLibraryRoot(session, controller("com.example.remote"), null).get()

            assertEquals(SessionError.ERROR_NOT_SUPPORTED, result.resultCode)
        } finally {
            session.release()
            player.release()
        }
    }

    @Test
    fun `resumption for playback and preview use separate sources`() {
        val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication()).setLooper(Looper.getMainLooper()).build()
        val session = MediaSession.Builder(RuntimeEnvironment.getApplication(), player).build()
        try {
            val full = MediaSession.MediaItemsWithStartPosition(listOf(MediaItem.EMPTY, MediaItem.EMPTY), 1, 5_000)
            val single = MediaSession.MediaItemsWithStartPosition(listOf(MediaItem.EMPTY), 0, 5_000)
            val callback = PlaybackSessionCallback(
                ownPackage = "own",
                editor = QueueEditor(player),
                resumption = { Futures.immediateFuture(full) },
                preview = { Futures.immediateFuture(single) },
            )
            val systemUi = controller("com.android.systemui")

            assertEquals(full, callback.onPlaybackResumption(session, systemUi, true).get())
            assertEquals(single, callback.onPlaybackResumption(session, systemUi, false).get())
        } finally {
            session.release()
            player.release()
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
