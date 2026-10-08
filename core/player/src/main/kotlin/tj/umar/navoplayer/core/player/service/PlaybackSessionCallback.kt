package tj.umar.navoplayer.core.player.service

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

internal class PlaybackSessionCallback : MediaSession.Callback {

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
    ): ListenableFuture<MutableList<MediaItem>> = Futures.immediateFuture(
        mediaItems.map { item ->
            item.buildUpon().setUri(item.requestMetadata.mediaUri).build()
        }.toMutableList(),
    )
}
