package tj.umar.navoplayer.core.player.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.player.mapper.queueItemId

@OptIn(UnstableApi::class)
internal class QueueEditor(private val player: ExoPlayer) {

    fun enqueue(items: List<MediaItem>, insertion: QueueInsertion): Boolean {
        if (items.isEmpty()) return false
        val wasEmpty = player.mediaItemCount == 0
        val hasEnded = player.playbackState == Player.STATE_ENDED
        val index = when {
            wasEmpty -> 0
            insertion == QueueInsertion.Next -> player.currentMediaItemIndex + 1
            else -> player.mediaItemCount
        }
        player.addMediaItems(index, items)
        when {
            wasEmpty -> {
                player.prepare()
                player.play()
            }
            hasEnded -> {
                player.seekToDefaultPosition(index)
                player.prepare()
                player.play()
            }
        }
        return true
    }

    fun remove(queueItemId: String): Boolean {
        val index = indexOf(queueItemId)
        if (index < 0 || index == player.currentMediaItemIndex) return false
        player.removeMediaItem(index)
        return true
    }

    fun move(queueItemId: String, toPlayIndex: Int): Boolean {
        val index = indexOf(queueItemId)
        if (index < 0) return false
        if (player.shuffleModeEnabled) {
            val order = player.currentTimeline.playOrder(shuffle = true)
            val from = order.indexOf(index)
            if (from < 0) return false
            player.setShuffleOrder(QueueShuffleOrder(order.withItemMoved(from, toPlayIndex)))
        } else {
            val target = toPlayIndex.coerceIn(0, player.mediaItemCount - 1)
            if (target != index) player.moveMediaItem(index, target)
        }
        return true
    }

    private fun indexOf(queueItemId: String): Int =
        (0 until player.mediaItemCount).firstOrNull { player.getMediaItemAt(it).queueItemId() == queueItemId } ?: -1
}
