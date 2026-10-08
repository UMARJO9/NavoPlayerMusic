package tj.umar.navoplayer.feature.playlists.addto

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaylistSummary

@Immutable
internal data class AddToPlaylistState(
    val trackIds: List<Long> = emptyList(),
    val isLoading: Boolean = true,
    val playlists: List<PlaylistSummary> = emptyList(),
    val loadFailed: Boolean = false,
    val isNameDialogVisible: Boolean = false,
    val isSaving: Boolean = false,
)

internal sealed interface AddToPlaylistIntent {
    data class Opened(val trackIds: List<Long>) : AddToPlaylistIntent
    data class PlaylistClicked(val playlistId: Long) : AddToPlaylistIntent
    data object NewPlaylistClicked : AddToPlaylistIntent
    data object NameDialogDismissed : AddToPlaylistIntent
    data class NewPlaylistConfirmed(val name: String) : AddToPlaylistIntent
    data object RetryLoad : AddToPlaylistIntent
    data object Dismissed : AddToPlaylistIntent
}

internal sealed interface AddToPlaylistEffect {
    data class Added(val playlistName: String, val addedCount: Int) : AddToPlaylistEffect
    data class Created(val playlistName: String) : AddToPlaylistEffect
    data object Failed : AddToPlaylistEffect
}
