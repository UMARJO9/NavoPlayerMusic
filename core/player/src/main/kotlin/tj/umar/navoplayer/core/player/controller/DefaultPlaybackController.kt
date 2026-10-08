package tj.umar.navoplayer.core.player.controller

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.mapper.toPlayerRepeatMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val STOP_SHARING_DELAY_MILLIS = 5_000L

@Singleton
internal class DefaultPlaybackController @Inject constructor(
    private val connection: MediaControllerConnection,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ApplicationScope scope: CoroutineScope,
) : PlaybackController {

    private val source = MutableStateFlow<PlaybackSource?>(null)

    private val playbackState: Flow<PlaybackState> = connection
        .withController { controller ->
            controller.events { _, _ -> true }
                .combine(source) { _, currentSource -> controller.toPlaybackState(currentSource) }
        }
        .distinctUntilChanged()
        .shareIn(scope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), replay = 1)

    private val progress: Flow<PlaybackProgress> = connection
        .withController { controller ->
            val playingChanges = controller.events { _, events ->
                events.containsAny(
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                )
            }.map { controller.isPlaying }
            progressTicks(playingChanges).map { controller.toProgress() }
        }
        .distinctUntilChanged()
        .shareIn(scope, SharingStarted.WhileSubscribed(STOP_SHARING_DELAY_MILLIS), replay = 1)

    override fun observePlaybackState(): Flow<PlaybackState> = playbackState

    override fun observeProgress(): Flow<PlaybackProgress> = progress

    override suspend fun play(queue: List<Track>, startIndex: Int, source: PlaybackSource) {
        val items = withContext(defaultDispatcher) { queue.map { it.toMediaItem() } }
        this.source.value = source
        connection.command { controller ->
            controller.setMediaItems(items, startIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    override suspend fun playShuffled(queue: List<Track>, source: PlaybackSource) {
        val items = withContext(defaultDispatcher) { queue.map { it.toMediaItem() } }
        this.source.value = source
        val startIndex = Random.nextInt(items.size)
        connection.command { controller ->
            controller.shuffleModeEnabled = true
            controller.setMediaItems(items, startIndex, 0L)
            controller.prepare()
            controller.play()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun togglePlayPause() {
        connection.command { controller -> Util.handlePlayPauseButtonAction(controller) }
    }

    override suspend fun skipToNext() {
        connection.command { controller -> controller.seekToNext() }
    }

    override suspend fun skipToPrevious() {
        connection.command { controller -> controller.seekToPrevious() }
    }

    override suspend fun seekTo(positionMs: Long) {
        connection.command { controller -> controller.seekTo(positionMs) }
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        connection.command { controller -> controller.shuffleModeEnabled = enabled }
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        connection.command { controller -> controller.repeatMode = mode.toPlayerRepeatMode() }
    }
}

private fun Player.events(filter: (Player, Player.Events) -> Boolean): Flow<Unit> = callbackFlow {
    val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (filter(player, events)) trySend(Unit)
        }
    }
    trySend(Unit)
    addListener(listener)
    awaitClose { removeListener(listener) }
}.conflate()
