package tj.umar.navoplayer.core.player.controller

import androidx.media3.common.Player
import androidx.media3.common.Timeline
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.player.mapper.toQueueItem
import tj.umar.navoplayer.core.player.service.playOrder

internal data class QueueSnapshot(
    val timeline: Timeline,
    val currentWindowIndex: Int,
    val shuffle: Boolean,
)

internal fun Player.queueSnapshot(): QueueSnapshot =
    QueueSnapshot(currentTimeline, currentMediaItemIndex, shuffleModeEnabled)

internal fun QueueSnapshot.toPlaybackQueue(): PlaybackQueue {
    if (timeline.isEmpty) return PlaybackQueue.Empty
    val order = timeline.playOrder(shuffle)
    val window = Timeline.Window()
    val items = order.map { index -> timeline.getWindow(index, window).mediaItem.toQueueItem(index) }
    return PlaybackQueue(items, order.indexOf(currentWindowIndex))
}
