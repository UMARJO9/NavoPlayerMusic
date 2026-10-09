package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class SkipToQueueItemUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(id: QueueItemId) = playbackController.skipToQueueItem(id)
}
