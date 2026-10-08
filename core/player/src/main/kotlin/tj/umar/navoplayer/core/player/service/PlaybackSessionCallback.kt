package tj.umar.navoplayer.core.player.service

import android.content.ContentResolver
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

internal class PlaybackSessionCallback : MediaSession.Callback {

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
    ): ListenableFuture<MutableList<MediaItem>> {
        val playable = resolvePlayableItems(mediaItems)
        return if (playable.isEmpty()) {
            Futures.immediateFailedFuture(UnsupportedOperationException())
        } else {
            Futures.immediateFuture(playable.toMutableList())
        }
    }
}

internal fun resolvePlayableItems(items: List<MediaItem>): List<MediaItem> = items.mapNotNull { item ->
    val uri = item.requestMetadata.mediaUri ?: item.localConfiguration?.uri
    if (uri == null || !uri.isContentUri()) return@mapNotNull null
    item.buildUpon().setUri(uri).build()
}

private fun Uri.isContentUri(): Boolean = scheme == ContentResolver.SCHEME_CONTENT
