package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class RemoveQueueItemUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(id: QueueItemId): Boolean {
        val queue = playbackController.observeQueue().first()
        val index = queue.indexOf(id)
        if (index < 0 || index == queue.currentIndex) return false
        return playbackController.removeQueueItem(id)
    }
}
