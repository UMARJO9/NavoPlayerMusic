package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class MoveQueueItemUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(id: QueueItemId, toIndex: Int): Boolean {
        val queue = playbackController.observeQueue().first()
        val from = queue.indexOf(id)
        if (from < 0) return false
        val target = toIndex.coerceIn(0, queue.items.lastIndex)
        if (target == from) return true
        return playbackController.moveQueueItem(id, target)
    }
}
