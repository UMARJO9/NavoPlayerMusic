package tj.umar.navoplayer.core.player.sleeptimer

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(UnstableApi::class)
internal class ExoSleepTimerPlayer(private val player: ExoPlayer) : SleepTimerPlayer {

    override val hasEnded: Boolean
        get() = player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED

    override val isPlaying: Boolean
        get() = player.isPlaying

    override var volume: Float
        get() = player.volume
        set(value) {
            player.volume = value
        }

    override val events: Flow<SleepTimerPlayerEvent> = callbackFlow {
        val listener = SleepTimerPlayerListener { trySend(it) }
        player.addListener(listener)
        awaitClose { player.removeListener(listener) }
    }

    override fun pause() {
        player.pause()
    }

    override fun setPauseAtEndOfMediaItems(enabled: Boolean) {
        player.pauseAtEndOfMediaItems = enabled
    }
}
