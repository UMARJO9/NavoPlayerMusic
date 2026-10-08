package tj.umar.navoplayer.feature.search.search

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroupKey

internal const val SEARCH_DEBOUNCE_MS = 200L
internal const val SEARCH_GROUP_LIMIT = 3

internal enum class SearchPhase { Idle, Loading, Results, NoResults, Error }

@Immutable
internal data class SearchState(
    val query: String = "",
    val phase: SearchPhase = SearchPhase.Idle,
    val resultsQuery: String = "",
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albumCount: Int = 0,
    val artistCount: Int = 0,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null
}

internal sealed interface SearchIntent {
    data class ScreenStarted(val hasPermission: Boolean) : SearchIntent
    data object ScreenStopped : SearchIntent
    data class QueryChanged(val query: String) : SearchIntent
    data object ClearQueryClicked : SearchIntent
    data object BackClicked : SearchIntent
    data class TrackClicked(val trackId: Long) : SearchIntent
    data class GroupClicked(val key: TrackGroupKey) : SearchIntent
    data object RetryClicked : SearchIntent
    data class TrackLongPressed(val trackId: Long) : SearchIntent
}

internal sealed interface SearchEffect {
    data object NavigateBack : SearchEffect
    data object NavigateToWelcome : SearchEffect
    data class NavigateToGroup(val key: TrackGroupKey) : SearchEffect
    data class OpenAddToPlaylist(val trackIds: List<Long>) : SearchEffect
}
