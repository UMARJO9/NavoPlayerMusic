package tj.umar.navoplayer.core.domain.model

data class PlaybackState(
    val currentTrack: Track?,
    val nextTrack: Track?,
    val previousTrack: Track? = null,
    val isPlaying: Boolean,
    val shuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
    val source: PlaybackSource?,
) {
    companion object {
        val Empty = PlaybackState(
            currentTrack = null,
            nextTrack = null,
            isPlaying = false,
            shuffleEnabled = false,
            repeatMode = RepeatMode.Off,
            source = null,
        )
    }
}
