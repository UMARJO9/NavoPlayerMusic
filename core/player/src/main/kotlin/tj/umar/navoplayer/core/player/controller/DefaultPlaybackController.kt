package tj.umar.navoplayer.core.player.controller

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.coroutines.ApplicationScope
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.player.mapper.toPlayerRepeatMode
import tj.umar.navoplayer.core.player.service.QueueRequest
import tj.umar.navoplayer.core.player.service.QueueSessionCommands
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TAG = "PlaybackController"
private const val STOP_SHARING_DELAY_MILLIS = 5_000L
private const val MAX_CONNECT_RETRIES = 3L
private const val RETRY_BACKOFF_MILLIS = 1_000L

@Singleton
internal class DefaultPlaybackController @Inject constructor(
    private val connection: MediaControllerConnection,
    private val idFactory: QueueItemIdFactory,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ApplicationScope scope: CoroutineScope,
) : PlaybackController {

    private val source = MutableStateFlow<PlaybackSource?>(null)

    private val sharing = SharingStarted.WhileSubscribed(
        stopTimeoutMillis = STOP_SHARING_DELAY_MILLIS,
        replayExpirationMillis = 0,
    )

    private val playbackState: Flow<PlaybackState> = connection
        .withController { controller ->
            controller.events { _, _ -> true }
                .combine(source) { _, currentSource -> controller.toPlaybackState(currentSource) }
        }
        .retryConnecting()
        .catch { failure ->
            Log.w(TAG, "Playback state unavailable", failure)
            emit(PlaybackState.Empty)
        }
        .distinctUntilChanged()
        .shareIn(scope, sharing, replay = 1)

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
        .retryConnecting()
        .catch { failure ->
            Log.w(TAG, "Playback progress unavailable", failure)
            emit(PlaybackProgress.Zero)
        }
        .distinctUntilChanged()
        .shareIn(scope, sharing, replay = 1)

    private val queue: Flow<PlaybackQueue> = connection
        .withController { controller ->
            controller.events { _, events ->
                events.containsAny(
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                )
            }.map { controller.queueSnapshot() }
        }
        .map { snapshot -> withContext(defaultDispatcher) { snapshot.toPlaybackQueue() } }
        .retryConnecting()
        .catch { failure ->
            Log.w(TAG, "Playback queue unavailable", failure)
            emit(PlaybackQueue.Empty)
        }
        .distinctUntilChanged()
        .shareIn(scope, sharing, replay = 1)

    override fun observePlaybackState(): Flow<PlaybackState> = playbackState

    override fun observeProgress(): Flow<PlaybackProgress> = progress

    override fun observeQueue(): Flow<PlaybackQueue> = queue

    override suspend fun play(queue: List<Track>, startIndex: Int, source: PlaybackSource) {
        val items = withContext(defaultDispatcher) { queue.map { it.toMediaItem(idFactory.create()) } }
        runCommand { controller ->
            controller.setMediaItems(items, startIndex, 0L)
            this.source.value = source
            controller.prepare()
            controller.play()
        }
    }

    override suspend fun playShuffled(queue: List<Track>, source: PlaybackSource) {
        val items = withContext(defaultDispatcher) { queue.map { it.toMediaItem(idFactory.create()) } }
        val startIndex = Random.nextInt(items.size)
        runCommand { controller ->
            controller.shuffleModeEnabled = true
            controller.setMediaItems(items, startIndex, 0L)
            this.source.value = source
            controller.prepare()
            controller.play()
        }
    }

    @OptIn(UnstableApi::class)
    override suspend fun togglePlayPause() {
        runCommand { controller -> Util.handlePlayPauseButtonAction(controller) }
    }

    override suspend fun skipToNext() {
        runCommand { controller -> controller.seekToNext() }
    }

    override suspend fun skipToPrevious() {
        runCommand { controller -> controller.seekToPrevious() }
    }

    override suspend fun seekTo(positionMs: Long) {
        runCommand { controller -> controller.seekTo(positionMs) }
    }

    override suspend fun setShuffleEnabled(enabled: Boolean) {
        runCommand { controller -> controller.shuffleModeEnabled = enabled }
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        runCommand { controller -> controller.repeatMode = mode.toPlayerRepeatMode() }
    }

    override suspend fun skipToQueueItem(id: QueueItemId) {
        runCommand { controller ->
            val index = (0 until controller.mediaItemCount)
                .firstOrNull { controller.getMediaItemAt(it).queueItemId() == id.value }
                ?: return@runCommand
            controller.seekToDefaultPosition(index)
            if (controller.playbackState == Player.STATE_IDLE) controller.prepare()
            controller.play()
        }
    }

    override suspend fun removeQueueItem(id: QueueItemId): Boolean =
        sendQueueCommand { QueueRequest.Remove(id.value) }

    override suspend fun moveQueueItem(id: QueueItemId, toIndex: Int): Boolean =
        sendQueueCommand { QueueRequest.Move(id.value, toIndex) }

    override suspend fun enqueue(tracks: List<Track>, insertion: QueueInsertion): Boolean =
        sendQueueCommand { QueueRequest.Enqueue(tracks.map { it.toMediaItem(idFactory.create()) }, insertion) }

    private suspend fun sendQueueCommand(request: () -> QueueRequest): Boolean = try {
        val encoded = withContext(defaultDispatcher) { QueueSessionCommands.encode(request()) }
        val result = connection.command { it.sendCustomCommand(encoded.command, encoded.args) }.await()
        val accepted = result.resultCode == SessionResult.RESULT_SUCCESS
        if (!accepted) Log.w(TAG, "Queue command rejected with code ${result.resultCode}")
        accepted
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Exception) {
        Log.w(TAG, "Queue command failed", failure)
        false
    }

    private suspend fun runCommand(block: (MediaController) -> Unit) {
        try {
            connection.command(block)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            Log.w(TAG, "Playback command failed", failure)
        }
    }
}

private fun <T> Flow<T>.retryConnecting(): Flow<T> = retryWhen { cause, attempt ->
    if (cause is CancellationException || attempt >= MAX_CONNECT_RETRIES) {
        false
    } else {
        delay(RETRY_BACKOFF_MILLIS * (attempt + 1))
        true
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
