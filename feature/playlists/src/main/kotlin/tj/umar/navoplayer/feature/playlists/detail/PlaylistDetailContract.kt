package tj.umar.navoplayer.feature.playlists.detail

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.isPlaylist

@Immutable
internal data class PlaylistDetailState(
    val playlistId: Long,
    val isLoading: Boolean = true,
    val playlist: PlaylistDetail? = null,
    val totalMinutes: Int = 0,
    val loadFailed: Boolean = false,
    val isMissing: Boolean = false,
    val isDeleting: Boolean = false,
    val dialog: PlaylistDetailDialog? = null,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null

    val tracks: List<Track>
        get() = playlist?.tracks.orEmpty()

    val isPlaylistActive: Boolean
        get() = currentTrackId != null && currentSource.isPlaylist(playlistId)

    val isPlaylistPlaying: Boolean
        get() = isPlaylistActive && isPlaying
}

@Immutable
internal sealed interface PlaylistDetailDialog {
    data class Rename(val currentName: String) : PlaylistDetailDialog
    data object ConfirmDelete : PlaylistDetailDialog
    data class ConfirmRemoveTrack(val trackId: Long, val title: String) : PlaylistDetailDialog
}

internal sealed interface PlaylistDetailIntent {
    data class ScreenStarted(val hasPermission: Boolean) : PlaylistDetailIntent
    data object ScreenStopped : PlaylistDetailIntent
    data object RetryLoad : PlaylistDetailIntent
    data object BackClicked : PlaylistDetailIntent
    data object PlayClicked : PlaylistDetailIntent
    data object ShuffleClicked : PlaylistDetailIntent
    data class TrackClicked(val trackId: Long) : PlaylistDetailIntent
    data class TrackLongPressed(val trackId: Long) : PlaylistDetailIntent
    data object RenameClicked : PlaylistDetailIntent
    data class RenameConfirmed(val name: String) : PlaylistDetailIntent
    data object DeleteClicked : PlaylistDetailIntent
    data object DeleteConfirmed : PlaylistDetailIntent
    data object RemoveTrackConfirmed : PlaylistDetailIntent
    data object DialogDismissed : PlaylistDetailIntent
}

internal sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data object NavigateToWelcome : PlaylistDetailEffect
    data class ShowMessage(val message: PlaylistDetailMessage) : PlaylistDetailEffect
}

internal enum class PlaylistDetailMessage { RenameFailed, DeleteFailed, RemoveFailed }
