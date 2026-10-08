package tj.umar.navoplayer.feature.playlists.addto

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaylistSummary

@Immutable
internal data class AddToPlaylistState(
    val token: Long? = null,
    val trackIds: List<Long> = emptyList(),
    val isLoading: Boolean = true,
    val playlists: List<PlaylistSummary> = emptyList(),
    val loadFailed: Boolean = false,
    val isNameFormVisible: Boolean = false,
    val isSaving: Boolean = false,
)

internal sealed interface AddToPlaylistIntent {
    data class Opened(val request: AddToPlaylistRequest) : AddToPlaylistIntent
    data class PlaylistClicked(val playlistId: Long) : AddToPlaylistIntent
    data object NewPlaylistClicked : AddToPlaylistIntent
    data object NameFormDismissed : AddToPlaylistIntent
    data class NewPlaylistConfirmed(val name: String) : AddToPlaylistIntent
    data object RetryLoad : AddToPlaylistIntent
    data object Dismissed : AddToPlaylistIntent
}

internal sealed interface AddToPlaylistEffect {
    val token: Long

    data class Added(override val token: Long, val playlistName: String, val addedCount: Int) : AddToPlaylistEffect
    data class Created(override val token: Long, val playlistName: String) : AddToPlaylistEffect
    data class Failed(override val token: Long) : AddToPlaylistEffect
}
