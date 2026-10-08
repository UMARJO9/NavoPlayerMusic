package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class ToggleShuffleUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke() {
        val enabled = playbackController.observePlaybackState().first().shuffleEnabled
        playbackController.setShuffleEnabled(!enabled)
    }
}
