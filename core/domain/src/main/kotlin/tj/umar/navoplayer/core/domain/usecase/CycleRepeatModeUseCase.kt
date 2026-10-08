package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class CycleRepeatModeUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke() {
        val mode = playbackController.observePlaybackState().first().repeatMode
        playbackController.setRepeatMode(mode.next())
    }
}
