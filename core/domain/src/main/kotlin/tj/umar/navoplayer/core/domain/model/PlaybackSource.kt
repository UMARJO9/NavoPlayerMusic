package tj.umar.navoplayer.core.domain.model

sealed interface PlaybackSource {
    data object AllTracks : PlaybackSource
}
