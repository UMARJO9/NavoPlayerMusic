package tj.umar.navoplayer.feature.library.library

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.FavoritesSummary
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.GroupSort
import tj.umar.navoplayer.core.domain.model.GroupSortField
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.model.SortDirection
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackSort
import tj.umar.navoplayer.core.domain.model.TrackSortField
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
    val favorites: FavoritesSummary = FavoritesSummary.Empty,
    val isLoadingPlaylists: Boolean = true,
    val playlistsLoadFailed: Boolean = false,
    val isCreatePlaylistSheetVisible: Boolean = false,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
    val trackSort: TrackSort = TrackSort.Default,
    val groupSort: GroupSort = GroupSort.Default,
    val sortSheet: SortTarget? = null,
    val pendingTrackSort: TrackSort? = null,
    val pendingGroupSort: GroupSort? = null,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null

    val shownTrackSort: TrackSort
        get() = pendingTrackSort ?: trackSort

    val shownGroupSort: GroupSort
        get() = pendingGroupSort ?: groupSort
}

internal enum class SortTarget { Tracks, Groups }

internal sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
    data class ScreenStarted(val hasPermission: Boolean) : LibraryIntent
    data object ScreenStopped : LibraryIntent
    data object RetryLoadTracks : LibraryIntent
    data class TrackClicked(val trackId: Long) : LibraryIntent
    data class GroupClicked(val key: TrackGroupKey) : LibraryIntent
    data object SearchClicked : LibraryIntent
    data object SettingsClicked : LibraryIntent
    data class SortClicked(val target: SortTarget) : LibraryIntent
    data object SortSheetDismissed : LibraryIntent
    data class TrackSortFieldSelected(val field: TrackSortField) : LibraryIntent
    data class GroupSortFieldSelected(val field: GroupSortField) : LibraryIntent
    data class SortDirectionSelected(val target: SortTarget, val direction: SortDirection) : LibraryIntent
    data object ShuffleClicked : LibraryIntent
    data class PlaylistClicked(val playlistId: Long) : LibraryIntent
    data object FavoritesClicked : LibraryIntent
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
    data object NavigateToFavorites : LibraryEffect
    data object NavigateToSettings : LibraryEffect
    data object ShowCreatePlaylistFailed : LibraryEffect
    data object ShowSortSaveFailed : LibraryEffect
    data class OpenTrackActions(val trackIds: List<Long>) : LibraryEffect
}
