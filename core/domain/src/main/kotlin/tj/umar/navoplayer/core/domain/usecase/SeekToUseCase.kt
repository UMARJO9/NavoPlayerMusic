package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class SeekToUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(positionMs: Long) {
        playbackController.seekTo(positionMs.coerceAtLeast(0))
    }
}
