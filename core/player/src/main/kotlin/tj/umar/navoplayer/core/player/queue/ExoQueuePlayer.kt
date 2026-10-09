package tj.umar.navoplayer.core.player.queue

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.service.playOrder

internal class ExoQueuePlayer(
    private val player: ExoPlayer,
    private val sourceStore: PlaybackSourceStore,
    private val pending: PendingShuffleOrder,
) : QueuePlayer {

    override val hasCurrentItem: Boolean
        get() = player.currentMediaItem != null

    override val isPlaying: Boolean
        get() = player.isPlaying

    override val events: Flow<QueuePlayerEvent> = callbackFlow {
        val listener = object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                if (reason != Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED) return
                trySend(if (timeline.isEmpty) QueuePlayerEvent.Emptied else QueuePlayerEvent.StructureChanged)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                trySend(QueuePlayerEvent.StructureChanged)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                trySend(QueuePlayerEvent.ProgressChanged)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int,
            ) {
                trySend(QueuePlayerEvent.ProgressChanged)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                trySend(QueuePlayerEvent.ProgressChanged)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                trySend(QueuePlayerEvent.PlayingChanged(isPlaying))
            }
        }
        player.addListener(listener)
        awaitClose { player.removeListener(listener) }
    }.buffer(Channel.UNLIMITED)

    override fun capture(): CapturedQueue? = player.captureQueue(sourceStore.source.value)

    override fun apply(queue: RestoredMediaQueue) {
        prepareForResumption(queue)
        player.setMediaItems(queue.items, queue.startIndex, queue.startPositionMs)
    }

    override fun prepareForResumption(queue: RestoredMediaQueue) {
        sourceStore.set(queue.source)
        player.repeatMode = queue.repeatMode
        val order = queue.shuffleOrder
        val firstId = queue.items.firstOrNull()?.queueItemId()
        if (queue.shuffleEnabled && order != null && firstId != null) pending.offer(order, firstId, queue.items.size)
        player.shuffleModeEnabled = queue.shuffleEnabled
    }

    override fun currentPreview(): MediaSession.MediaItemsWithStartPosition? {
        val item = player.currentMediaItem ?: return null
        return MediaSession.MediaItemsWithStartPosition(listOf(item), 0, player.currentPosition)
    }

    override fun currentResumption(): MediaSession.MediaItemsWithStartPosition? {
        if (player.mediaItemCount == 0) return null
        val items = (0 until player.mediaItemCount).map(player::getMediaItemAt)
        val firstId = items.first().queueItemId()
        if (player.shuffleModeEnabled && firstId != null) {
            pending.offer(player.currentTimeline.playOrder(shuffle = true), firstId, items.size)
        }
        return MediaSession.MediaItemsWithStartPosition(items, player.currentMediaItemIndex, player.currentPosition)
    }
}
