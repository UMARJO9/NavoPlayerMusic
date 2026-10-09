package tj.umar.navoplayer.feature.player.queue

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.QueueItemId

@Immutable
internal data class QueueState(
    val isLoading: Boolean = true,
    val items: List<QueueItem> = emptyList(),
    val currentItemId: QueueItemId? = null,
    val isPlaying: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val source: PlaybackSource? = null,
) {
    val currentIndex: Int
        get() = items.indexOfFirst { it.id == currentItemId }
}

internal sealed interface QueueIntent {
    data object ScreenStarted : QueueIntent
    data object ScreenStopped : QueueIntent
    data class ItemClicked(val id: QueueItemId) : QueueIntent
    data class RemoveClicked(val id: QueueItemId) : QueueIntent
    data class MoveItem(val id: QueueItemId, val toIndex: Int) : QueueIntent
    data object CloseClicked : QueueIntent
}

internal sealed interface QueueEffect {
    data object Close : QueueEffect
}
