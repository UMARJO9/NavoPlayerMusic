package tj.umar.navoplayer.core.player.queue

import androidx.media3.common.MediaItem
import androidx.media3.session.MediaSession
import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.PlaybackSource

internal interface QueuePlayer {
    val hasCurrentItem: Boolean
    val isPlaying: Boolean
    val events: Flow<QueuePlayerEvent>
    fun capture(): CapturedQueue?
    fun apply(queue: RestoredMediaQueue)
    fun prepareForResumption(queue: RestoredMediaQueue)
    fun currentResumption(): MediaSession.MediaItemsWithStartPosition?
    fun currentPreview(): MediaSession.MediaItemsWithStartPosition?
}

internal sealed interface QueuePlayerEvent {
    data object StructureChanged : QueuePlayerEvent
    data object ProgressChanged : QueuePlayerEvent
    data object Emptied : QueuePlayerEvent
    data class PlayingChanged(val isPlaying: Boolean) : QueuePlayerEvent
}

internal class RestoredMediaQueue(
    val items: List<MediaItem>,
    val startIndex: Int,
    val startPositionMs: Long,
    val shuffleOrder: IntArray?,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
    val source: PlaybackSource?,
)
