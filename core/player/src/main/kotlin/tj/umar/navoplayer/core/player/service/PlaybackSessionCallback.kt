package tj.umar.navoplayer.core.player.service

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
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
    private val resumption: () -> ListenableFuture<MediaSession.MediaItemsWithStartPosition> = {
        Futures.immediateFailedFuture(UnsupportedOperationException())
    },
    private val preview: () -> ListenableFuture<MediaSession.MediaItemsWithStartPosition> = {
        Futures.immediateFailedFuture(UnsupportedOperationException())
    },
    private val newQueueItemId: () -> String = { UUID.randomUUID().toString() },
) : MediaLibraryService.MediaLibrarySession.Callback {

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
    ): MediaSession.ConnectionResult = MediaSession.ConnectionResult.AcceptedResultBuilder(session)
        .setAvailableSessionCommands(sessionCommandsFor(controller.packageName, controller.isTrusted, ownPackage))
        .build()

    override fun onGetLibraryRoot(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<MediaItem>> =
        Futures.immediateFuture(LibraryResult.ofError(SessionError.ERROR_NOT_SUPPORTED))

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

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean,
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = if (isForPlayback) resumption() else preview()

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
internal fun sessionCommandsFor(packageName: String, isTrusted: Boolean, ownPackage: String): SessionCommands {
    val defaults = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
    return when {
        packageName == ownPackage -> defaults.buildUpon().apply { QueueSessionCommands.all.forEach(::add) }.build()
        packageName == SYSTEM_UI_PACKAGE && isTrusted ->
            defaults.buildUpon().apply { resumptionLibraryCommands.forEach(::add) }.build()
        else -> defaults
    }
}

private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

private val resumptionLibraryCommands = listOf(
    SessionCommand.COMMAND_CODE_LIBRARY_GET_LIBRARY_ROOT,
    SessionCommand.COMMAND_CODE_LIBRARY_SUBSCRIBE,
    SessionCommand.COMMAND_CODE_LIBRARY_UNSUBSCRIBE,
    SessionCommand.COMMAND_CODE_LIBRARY_GET_CHILDREN,
)

internal fun resolvePlayableItems(items: List<MediaItem>): List<MediaItem> = items.mapNotNull { item ->
    val uri = item.requestMetadata.mediaUri ?: item.localConfiguration?.uri
    if (uri == null || !uri.isContentUri()) return@mapNotNull null
    item.buildUpon().setUri(uri).build()
}

private fun Uri.isContentUri(): Boolean = scheme == ContentResolver.SCHEME_CONTENT
