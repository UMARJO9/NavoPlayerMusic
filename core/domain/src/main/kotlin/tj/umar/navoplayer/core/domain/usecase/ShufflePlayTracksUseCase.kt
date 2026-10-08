package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class ShufflePlayTracksUseCase @Inject constructor(
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(tracks: List<Track>, source: PlaybackSource) {
        if (tracks.isEmpty()) return
        playbackController.playShuffled(tracks, source)
    }
}
