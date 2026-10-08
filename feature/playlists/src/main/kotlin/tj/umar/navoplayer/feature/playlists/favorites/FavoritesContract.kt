package tj.umar.navoplayer.feature.playlists.favorites

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.isFavorites

@Immutable
internal data class FavoritesState(
    val isLoading: Boolean = true,
    val favorites: FavoriteTracks? = null,
    val totalMinutes: Int = 0,
    val loadFailed: Boolean = false,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null

    val tracks: List<Track>
        get() = favorites?.tracks.orEmpty()

    val isFavoritesActive: Boolean
        get() = currentTrackId != null && currentSource.isFavorites()

    val isFavoritesPlaying: Boolean
        get() = isFavoritesActive && isPlaying
}

internal sealed interface FavoritesIntent {
    data class ScreenStarted(val hasPermission: Boolean) : FavoritesIntent
    data object ScreenStopped : FavoritesIntent
    data object RetryLoad : FavoritesIntent
    data object BackClicked : FavoritesIntent
    data object PlayClicked : FavoritesIntent
    data object ShuffleClicked : FavoritesIntent
    data class TrackClicked(val trackId: Long) : FavoritesIntent
    data class TrackLongPressed(val trackId: Long) : FavoritesIntent
}

internal sealed interface FavoritesEffect {
    data object NavigateBack : FavoritesEffect
    data object NavigateToWelcome : FavoritesEffect
    data class TrackRemoved(val title: String) : FavoritesEffect
    data object RemoveFailed : FavoritesEffect
}
