package tj.umar.navoplayer.core.player.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.common.dispatchers.MainDispatcher
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    internal lateinit var noisyPolicy: AudioBecomingNoisyPolicy

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    private var serviceScope: CoroutineScope? = null

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val builder = MediaSession.Builder(this, player).setCallback(PlaybackSessionCallback())
        launchIntent()?.let(builder::setSessionActivity)
        mediaSession = builder.build()
        player.addListener(ShuffleOrderListener(player))
        val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
        serviceScope = scope
        noisyPolicy.pauseOnDisconnect().onEach(player::setHandleAudioBecomingNoisy).launchIn(scope)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val sessionPlayer = mediaSession?.player
        val stillPlaying = sessionPlayer != null &&
            sessionPlayer.playWhenReady &&
            sessionPlayer.mediaItemCount > 0 &&
            sessionPlayer.playbackState != Player.STATE_ENDED &&
            sessionPlayer.playbackState != Player.STATE_IDLE
        if (!stillPlaying) stopSelf()
    }

    override fun onDestroy() {
        serviceScope?.cancel()
        serviceScope = null
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }

    private fun launchIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
