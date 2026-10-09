package tj.umar.navoplayer.feature.library.groupdetail

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.model.TrackGroup
import tj.umar.navoplayer.core.domain.model.TrackGroupKey

@Immutable
internal data class GroupDetailState(
    val key: TrackGroupKey,
    val isLoading: Boolean = true,
    val group: TrackGroup? = null,
    val totalMinutes: Int = 0,
    val loadFailed: Boolean = false,
    val isMissing: Boolean = false,
    val currentTrackId: Long? = null,
    val currentSource: PlaybackSource? = null,
    val isPlaying: Boolean = false,
) {
    val hasActivePlayback: Boolean
        get() = currentTrackId != null

    val tracks: List<Track>
        get() = group?.tracks.orEmpty()
}

internal sealed interface GroupDetailIntent {
    data class ScreenStarted(val hasPermission: Boolean) : GroupDetailIntent
    data object ScreenStopped : GroupDetailIntent
    data object RetryLoad : GroupDetailIntent
    data object BackClicked : GroupDetailIntent
    data class TrackClicked(val trackId: Long) : GroupDetailIntent
    data object ShuffleClicked : GroupDetailIntent
    data class TrackLongPressed(val trackId: Long) : GroupDetailIntent
}

internal sealed interface GroupDetailEffect {
    data object NavigateBack : GroupDetailEffect
    data object NavigateToWelcome : GroupDetailEffect
    data class OpenAddToPlaylist(val trackIds: List<Long>) : GroupDetailEffect
}
