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
import tj.umar.navoplayer.core.common.time.ElapsedRealtimeClock
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.player.sleeptimer.ExoSleepTimerPlayer
import tj.umar.navoplayer.core.player.sleeptimer.SleepTimerExecutor
import tj.umar.navoplayer.core.player.sleeptimer.SleepTimerStore
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var player: ExoPlayer

    @Inject
    internal lateinit var noisyPolicy: AudioBecomingNoisyPolicy

    @Inject
    @field:MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    @Inject
    internal lateinit var sleepTimerStore: SleepTimerStore

    @Inject
    @field:ElapsedRealtimeClock
    lateinit var elapsedClock: NavoClock

    private var serviceScope: CoroutineScope? = null

    private var sleepTimerExecutor: SleepTimerExecutor? = null

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val builder = MediaSession.Builder(this, player).setCallback(PlaybackSessionCallback(packageName, QueueEditor(player)))
        launchIntent()?.let(builder::setSessionActivity)
        mediaSession = builder.build()
        player.addListener(ShuffleOrderListener(player))
        val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
        serviceScope = scope
        noisyPolicy.pauseOnDisconnect().onEach(player::setHandleAudioBecomingNoisy).launchIn(scope)
        sleepTimerStore.attach()
        sleepTimerExecutor = SleepTimerExecutor(sleepTimerStore, ExoSleepTimerPlayer(player), elapsedClock).also { it.start(scope) }
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
        sleepTimerExecutor?.release()
        sleepTimerExecutor = null
        sleepTimerStore.detach()
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
