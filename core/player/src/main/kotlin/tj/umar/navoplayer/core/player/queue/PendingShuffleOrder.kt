package tj.umar.navoplayer.core.player.queue

import androidx.media3.common.Player
import tj.umar.navoplayer.core.player.mapper.queueItemId

internal class PendingShuffleOrder {

    private var order: IntArray? = null
    private var firstQueueItemId: String? = null
    private var size: Int = 0

    fun offer(order: IntArray, firstQueueItemId: String, size: Int) {
        this.order = order.copyOf()
        this.firstQueueItemId = firstQueueItemId
        this.size = size
    }

    fun take(player: Player): IntArray? {
        val pending = order ?: return null
        val matches = player.mediaItemCount == size &&
            size > 0 &&
            player.getMediaItemAt(0).queueItemId() == firstQueueItemId
        order = null
        firstQueueItemId = null
        size = 0
        return if (matches) pending else null
    }
}
