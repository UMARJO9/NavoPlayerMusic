package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.domain.model.RepeatMode

object TestPlaybackStates {

    val playingAlpha = PlaybackState(
        currentTrack = TestTracks.alpha,
        nextTrack = TestTracks.beta,
        isPlaying = true,
        shuffleEnabled = false,
        repeatMode = RepeatMode.Off,
        source = PlaybackSource.AllTracks,
    )

    val pausedAlpha = playingAlpha.copy(isPlaying = false)
}
