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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.common.dispatchers.MainDispatcher
import tj.umar.navoplayer.core.common.time.ElapsedRealtimeClock
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.domain.usecase.LoadResumableQueueUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveEqualizerUseCase
import tj.umar.navoplayer.core.player.equalizer.AndroidSoundEffects
import tj.umar.navoplayer.core.player.equalizer.EqualizerApplier
import tj.umar.navoplayer.core.player.equalizer.EqualizerCapabilitiesStore
import tj.umar.navoplayer.core.player.equalizer.audioSessionIds
import tj.umar.navoplayer.core.player.nowplaying.NowPlayingPublisher
import tj.umar.navoplayer.core.player.nowplaying.SessionNowPlayingStore
import tj.umar.navoplayer.core.player.nowplaying.nowPlayingChanges
import tj.umar.navoplayer.core.player.queue.ExoQueuePlayer
import tj.umar.navoplayer.core.player.queue.PendingShuffleOrder
import tj.umar.navoplayer.core.player.queue.PlaybackSourceStore
import tj.umar.navoplayer.core.player.queue.QueuePersister
import tj.umar.navoplayer.core.player.queue.QueueRestorer
import tj.umar.navoplayer.core.player.queue.QueueStateWriter
import tj.umar.navoplayer.core.player.sleeptimer.ExoSleepTimerPlayer
import tj.umar.navoplayer.core.player.sleeptimer.SleepTimerExecutor
import tj.umar.navoplayer.core.player.sleeptimer.SleepTimerStore
import javax.inject.Inject

private const val EQUALIZER_RETRY_DELAY_MILLIS = 5_000L

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

    @Inject
    lateinit var observeEqualizer: ObserveEqualizerUseCase

    @Inject
    internal lateinit var equalizerStore: EqualizerCapabilitiesStore

    @Inject
    internal lateinit var nowPlayingStore: SessionNowPlayingStore

    @Inject
    lateinit var loadResumableQueue: LoadResumableQueueUseCase

    @Inject
    internal lateinit var queueWriter: QueueStateWriter

    @Inject
    internal lateinit var sourceStore: PlaybackSourceStore

    @Inject
    @field:DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    private var serviceScope: CoroutineScope? = null

    private var sleepTimerExecutor: SleepTimerExecutor? = null

    private var equalizerApplier: EqualizerApplier? = null

    private var nowPlayingPublisher: NowPlayingPublisher? = null

    private var queueRestorer: QueueRestorer? = null

    private var queuePersister: QueuePersister? = null

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val scope = CoroutineScope(SupervisorJob() + mainDispatcher)
        serviceScope = scope
        val pendingShuffleOrder = PendingShuffleOrder()
        val queuePlayer = ExoQueuePlayer(player, sourceStore, pendingShuffleOrder)
        val restorer = QueueRestorer(loadResumableQueue, queuePlayer, defaultDispatcher).also { queueRestorer = it }
        val persister = QueuePersister(queuePlayer, queueWriter, defaultDispatcher).also { queuePersister = it }
        val builder = MediaSession.Builder(this, player).setCallback(PlaybackSessionCallback(packageName, QueueEditor(player)))
        launchIntent()?.let(builder::setSessionActivity)
        mediaSession = builder.build()
        player.addListener(ShuffleOrderListener(player, pending = pendingShuffleOrder))
        noisyPolicy.pauseOnDisconnect().onEach(player::setHandleAudioBecomingNoisy).launchIn(scope)
        sleepTimerStore.attach()
        sleepTimerExecutor = SleepTimerExecutor(sleepTimerStore, ExoSleepTimerPlayer(player), elapsedClock).also { it.start(scope) }
        scope.launch { equalizerStore.ensureProbed() }
        equalizerApplier = EqualizerApplier(
            status = observeEqualizer().retryWhen { _, _ ->
                emit(EqualizerStatus.Probing)
                delay(EQUALIZER_RETRY_DELAY_MILLIS)
                true
            },
            sessionIds = player.audioSessionIds(),
            factory = ::AndroidSoundEffects,
        ).also { it.start(scope) }
        nowPlayingPublisher = NowPlayingPublisher(player.nowPlayingChanges(), nowPlayingStore).also { it.start(scope) }
        restorer.start(scope)
        scope.launch {
            restorer.settled.first { it }
            persister.start(scope, saveNow = queuePlayer.hasCurrentItem)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        queuePersister?.flush()
        val sessionPlayer = mediaSession?.player
        val stillPlaying = sessionPlayer != null &&
            sessionPlayer.playWhenReady &&
            sessionPlayer.mediaItemCount > 0 &&
            sessionPlayer.playbackState != Player.STATE_ENDED &&
            sessionPlayer.playbackState != Player.STATE_IDLE
        if (!stillPlaying) stopSelf()
    }

    override fun onDestroy() {
        if (queueRestorer?.settled?.value == true) queuePersister?.flush()
        queuePersister?.release()
        queuePersister = null
        queueRestorer = null
        nowPlayingPublisher?.release()
        nowPlayingPublisher = null
        equalizerApplier?.release()
        equalizerApplier = null
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
