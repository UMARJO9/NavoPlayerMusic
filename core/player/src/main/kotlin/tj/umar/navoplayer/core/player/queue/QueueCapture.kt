package tj.umar.navoplayer.core.player.queue

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueItem
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.player.mapper.queueItemId
import tj.umar.navoplayer.core.player.mapper.toDomainRepeatMode
import tj.umar.navoplayer.core.player.service.playOrder

internal data class CapturedQueue(
    val timeline: Timeline,
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
    val ended: Boolean,
    val source: PlaybackSource?,
)

internal fun Player.captureQueue(source: PlaybackSource?): CapturedQueue? {
    if (currentTimeline.isEmpty) return null
    return CapturedQueue(
        timeline = currentTimeline,
        currentIndex = currentMediaItemIndex,
        positionMs = currentPosition.coerceAtLeast(0),
        shuffleEnabled = shuffleModeEnabled,
        repeatMode = repeatMode,
        ended = playbackState == Player.STATE_ENDED,
        source = source,
    )
}

internal fun CapturedQueue.toSavedQueue(): SavedQueue? {
    val window = Timeline.Window()
    val newIndexOf = IntArray(timeline.windowCount) { -1 }
    val items = mutableListOf<SavedQueueItem>()
    for (index in 0 until timeline.windowCount) {
        val mediaItem = timeline.getWindow(index, window).mediaItem
        val trackId = mediaItem.mediaId.toLongOrNull() ?: continue
        val queueItemId = mediaItem.queueItemId() ?: continue
        newIndexOf[index] = items.size
        items += SavedQueueItem(QueueItemId(queueItemId), trackId)
    }
    if (items.isEmpty()) return null
    val shuffleOrder = if (shuffleEnabled) {
        timeline.playOrder(shuffle = true).map { newIndexOf[it] }.filter { it >= 0 }.takeIf { it.size == items.size }
    } else {
        null
    }
    val current = newIndexOf.getOrNull(currentIndex)?.takeIf { it >= 0 } ?: 0
    return SavedQueue(
        items = items,
        shuffleOrder = shuffleOrder,
        progress = toProgress().copy(currentIndex = current),
        source = source,
    )
}

internal fun CapturedQueue.toProgress(): SavedQueueProgress {
    val window = Timeline.Window()
    val savedIndex = (0 until currentIndex.coerceIn(0, timeline.windowCount))
        .count { timeline.getWindow(it, window).mediaItem.isSavable() }
    return SavedQueueProgress(
        currentIndex = savedIndex,
        positionMs = if (ended) 0 else positionMs,
        shuffleEnabled = shuffleEnabled,
        repeatMode = repeatMode.toDomainRepeatMode(),
    )
}

private fun MediaItem.isSavable(): Boolean = mediaId.toLongOrNull() != null && queueItemId() != null
