package tj.umar.navoplayer.core.player.service

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import tj.umar.navoplayer.core.player.mapper.withQueueItemId
import java.util.UUID

@OptIn(UnstableApi::class)
internal class PlaybackSessionCallback(
    private val ownPackage: String,
    private val editor: QueueEditor,
    private val newQueueItemId: () -> String = { UUID.randomUUID().toString() },
) : MediaSession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult = MediaSession.ConnectionResult.AcceptedResultBuilder(session)
        .setAvailableSessionCommands(sessionCommandsFor(controller.packageName, ownPackage))
        .build()

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
    ): ListenableFuture<SessionResult> {
        if (controller.packageName != ownPackage) {
            return Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
        }
        return Futures.immediateFuture(SessionResult(handleQueueCommand(customCommand, args)))
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
    ): ListenableFuture<MutableList<MediaItem>> {
        val playable = resolvePlayableItems(mediaItems).map { it.withQueueItemId(newQueueItemId) }
        return if (playable.isEmpty()) {
            Futures.immediateFailedFuture(UnsupportedOperationException())
        } else {
            Futures.immediateFuture(playable.toMutableList())
        }
    }

    fun handleQueueCommand(command: SessionCommand, args: Bundle): Int {
        val handled = when (val request = QueueSessionCommands.decode(command, args)) {
            null -> false
            is QueueRequest.Enqueue -> editor.enqueue(resolvePlayableItems(request.items), request.insertion)
            is QueueRequest.Remove -> editor.remove(request.queueItemId)
            is QueueRequest.Move -> editor.move(request.queueItemId, request.toIndex)
        }
        return if (handled) SessionResult.RESULT_SUCCESS else SessionError.ERROR_BAD_VALUE
    }
}

@OptIn(UnstableApi::class)
internal fun sessionCommandsFor(packageName: String, ownPackage: String): SessionCommands {
    val defaults = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
    if (packageName != ownPackage) return defaults
    return defaults.buildUpon().apply { QueueSessionCommands.all.forEach(::add) }.build()
}

internal fun resolvePlayableItems(items: List<MediaItem>): List<MediaItem> = items.mapNotNull { item ->
    val uri = item.requestMetadata.mediaUri ?: item.localConfiguration?.uri
    if (uri == null || !uri.isContentUri()) return@mapNotNull null
    item.buildUpon().setUri(uri).build()
}

private fun Uri.isContentUri(): Boolean = scheme == ContentResolver.SCHEME_CONTENT
