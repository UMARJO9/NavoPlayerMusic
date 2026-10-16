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
    val tracks: List<Track> = emptyList(),
    val orderOverride: List<Long>? = null,
    val removingTrackIds: Set<Long> = emptySet(),
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

    val canEdit: Boolean
        get() = playlist != null && !isDeleting

    val isPlaylistActive: Boolean
        get() = currentTrackId != null && currentSource.isPlaylist(playlistId)

    val isPlaylistPlaying: Boolean
        get() = isPlaylistActive && isPlaying
}

@Immutable
internal sealed interface PlaylistDetailDialog {
    data class Rename(val currentName: String) : PlaylistDetailDialog
    data object ConfirmDelete : PlaylistDetailDialog
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
    data class MoveTrack(val trackId: Long, val toIndex: Int) : PlaylistDetailIntent
    data class RemoveTrackRequested(val trackId: Long) : PlaylistDetailIntent
    data class UndoRemoveClicked(val token: Long) : PlaylistDetailIntent
    data class UndoRemoveDismissed(val token: Long) : PlaylistDetailIntent
    data object RenameClicked : PlaylistDetailIntent
    data class RenameConfirmed(val name: String) : PlaylistDetailIntent
    data object DeleteClicked : PlaylistDetailIntent
    data object DeleteConfirmed : PlaylistDetailIntent
    data object DialogDismissed : PlaylistDetailIntent
}

internal sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data object NavigateToWelcome : PlaylistDetailEffect
    data class ShowMessage(val message: PlaylistDetailMessage) : PlaylistDetailEffect
    data class ShowRemoveUndo(val token: Long, val title: String) : PlaylistDetailEffect
}

internal enum class PlaylistDetailMessage { RenameFailed, DeleteFailed, RemoveFailed, RestoreFailed, ReorderFailed }
