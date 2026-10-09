package tj.umar.navoplayer.core.player.nowplaying

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.player.mapper.toTrack

@OptIn(UnstableApi::class)
internal fun Player.toNowPlaying(): NowPlaying {
    val track = currentMediaItem?.toTrack() ?: return NowPlaying.Idle
    return NowPlaying(track = track, isPlaying = !Util.shouldShowPlayButton(this))
}

internal fun Player.nowPlayingChanges(): Flow<NowPlaying> = callbackFlow {
    val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(*NOW_PLAYING_EVENTS)) trySend(player.toNowPlaying())
        }
    }
    trySend(toNowPlaying())
    addListener(listener)
    awaitClose { removeListener(listener) }
}.distinctUntilChanged()

private val NOW_PLAYING_EVENTS = intArrayOf(
    Player.EVENT_MEDIA_ITEM_TRANSITION,
    Player.EVENT_TIMELINE_CHANGED,
    Player.EVENT_IS_PLAYING_CHANGED,
    Player.EVENT_PLAY_WHEN_READY_CHANGED,
    Player.EVENT_PLAYBACK_STATE_CHANGED,
    Player.EVENT_MEDIA_METADATA_CHANGED,
)
