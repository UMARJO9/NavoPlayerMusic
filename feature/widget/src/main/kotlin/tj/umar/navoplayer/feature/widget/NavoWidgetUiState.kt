package tj.umar.navoplayer.feature.widget

import tj.umar.navoplayer.core.domain.model.ArtworkKey
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.model.artworkKey

internal sealed interface NavoWidgetUiState {
    data object Empty : NavoWidgetUiState
    data class Playing(
        val trackId: Long,
        val title: String?,
        val artist: String?,
        val isPlaying: Boolean,
        val artworkKey: ArtworkKey,
    ) : NavoWidgetUiState
}

internal fun NowPlaying.toWidgetUiState(): NavoWidgetUiState {
    val current = track ?: return NavoWidgetUiState.Empty
    return NavoWidgetUiState.Playing(
        trackId = current.id,
        title = current.title.takeIf { it.isNotBlank() },
        artist = current.artist?.takeIf { it.isNotBlank() },
        isPlaying = isPlaying,
        artworkKey = current.artworkKey,
    )
}
