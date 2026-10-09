package tj.umar.navoplayer.core.player.equalizer

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(UnstableApi::class)
internal fun ExoPlayer.audioSessionIds(): Flow<Int> = callbackFlow {
    val listener = object : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            trySend(audioSessionId)
        }
    }
    trySend(audioSessionId)
    addListener(listener)
    awaitClose { removeListener(listener) }
}
    .filter { it != C.AUDIO_SESSION_ID_UNSET }
    .distinctUntilChanged()
