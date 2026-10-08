package tj.umar.navoplayer.feature.library.library

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.feature.library.R

internal enum class LibraryTab(
    @param:StringRes val titleRes: Int,
) {
    Tracks(R.string.library_tab_tracks),
    Playlists(R.string.library_tab_playlists),
    Albums(R.string.library_tab_albums),
    Artists(R.string.library_tab_artists),
    Folders(R.string.library_tab_folders),
}

@Immutable
internal data class LibraryState(
    val selectedTab: LibraryTab = LibraryTab.Tracks,
    val isLoadingTracks: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val totalMinutes: Int = 0,
    val tracksLoadFailed: Boolean = false,
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList(),
    val isLoadingPlaylists: Boolean = true,
    val playlistsLoadFailed: Boolean = false,
    val isCreatePlaylistSheetVisible: Boolean = false,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null
}

internal sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
    data class ScreenStarted(val hasPermission: Boolean) : LibraryIntent
    data object ScreenStopped : LibraryIntent
    data object RetryLoadTracks : LibraryIntent
    data class TrackClicked(val trackId: Long) : LibraryIntent
    data class GroupClicked(val key: TrackGroupKey) : LibraryIntent
    data object SearchClicked : LibraryIntent
    data object SettingsClicked : LibraryIntent
    data object SortClicked : LibraryIntent
    data object ShuffleClicked : LibraryIntent
    data class PlaylistClicked(val playlistId: Long) : LibraryIntent
    data object CreatePlaylistClicked : LibraryIntent
    data object CreatePlaylistDismissed : LibraryIntent
    data class CreatePlaylistConfirmed(val name: String) : LibraryIntent
    data object RetryLoadPlaylists : LibraryIntent
    data class TrackLongPressed(val trackId: Long) : LibraryIntent
}

internal sealed interface LibraryEffect {
    data object NavigateToWelcome : LibraryEffect
    data object NavigateToSearch : LibraryEffect
    data class NavigateToGroup(val key: TrackGroupKey) : LibraryEffect
    data class NavigateToPlaylist(val playlistId: Long) : LibraryEffect
    data object ShowCreatePlaylistFailed : LibraryEffect
    data class OpenAddToPlaylist(val trackIds: List<Long>) : LibraryEffect
}
