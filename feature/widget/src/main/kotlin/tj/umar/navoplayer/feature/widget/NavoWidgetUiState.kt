package tj.umar.navoplayer.feature.widget

import tj.umar.navoplayer.core.domain.model.NowPlaying

internal sealed interface NavoWidgetUiState {
    data object Empty : NavoWidgetUiState
    data class Playing(
        val trackId: Long,
        val title: String?,
        val artist: String?,
        val isPlaying: Boolean,
    ) : NavoWidgetUiState
}

internal fun NowPlaying.toWidgetUiState(): NavoWidgetUiState {
    val current = track ?: return NavoWidgetUiState.Empty
    return NavoWidgetUiState.Playing(
        trackId = current.id,
        title = current.title.takeIf { it.isNotBlank() },
        artist = current.artist?.takeIf { it.isNotBlank() },
        isPlaying = isPlaying,
    )
}
