package tj.umar.navoplayer.feature.player.trackactions

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.model.Track

@Immutable
internal data class TrackActionsState(
    val token: Long? = null,
    val trackIds: List<Long> = emptyList(),
    val isLoading: Boolean = true,
    val tracks: List<Track> = emptyList(),
    val isWorking: Boolean = false,
) {
    val track: Track?
        get() = tracks.singleOrNull()
}

internal sealed interface TrackActionsIntent {
    data class Opened(val request: TrackActionsRequest) : TrackActionsIntent
    data object PlayNextClicked : TrackActionsIntent
    data object AddToQueueClicked : TrackActionsIntent
    data object AddToPlaylistClicked : TrackActionsIntent
}

internal sealed interface TrackActionsEffect {
    val token: Long

    data class Enqueued(override val token: Long, val insertion: QueueInsertion, val count: Int) : TrackActionsEffect
    data class StartedPlayback(override val token: Long) : TrackActionsEffect
    data class Failed(override val token: Long) : TrackActionsEffect
    data class OpenAddToPlaylist(override val token: Long, val trackIds: List<Long>) : TrackActionsEffect
}
