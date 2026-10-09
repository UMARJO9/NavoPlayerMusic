package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.PlaybackQueue
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class ObservePlaybackQueueUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    operator fun invoke(): Flow<PlaybackQueue> = playbackController.observeQueue()
}
