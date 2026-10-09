package tj.umar.navoplayer.core.domain.model

data class SavedQueueItem(val queueItemId: QueueItemId, val trackId: Long)

data class SavedQueueProgress(
    val currentIndex: Int,
    val positionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
)

data class SavedQueue(
    val items: List<SavedQueueItem>,
    val shuffleOrder: List<Int>?,
    val progress: SavedQueueProgress,
    val source: PlaybackSource?,
)

data class ResumableQueue(
    val items: List<QueueItem>,
    val shuffleOrder: List<Int>?,
    val startIndex: Int,
    val startPositionMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: RepeatMode,
    val source: PlaybackSource?,
)

sealed interface QueueResumeResult {
    data class Resumable(val queue: ResumableQueue) : QueueResumeResult
    data object NothingSaved : QueueResumeResult
    data object AccessDenied : QueueResumeResult
    data object AllTracksMissing : QueueResumeResult
    data object Failed : QueueResumeResult
}
