package tj.umar.navoplayer.core.domain.model

@JvmInline
value class QueueItemId(val value: String)

data class QueueItem(val id: QueueItemId, val track: Track)

data class PlaybackQueue(val items: List<QueueItem>, val currentIndex: Int) {

    val current: QueueItem?
        get() = items.getOrNull(currentIndex)

    fun indexOf(id: QueueItemId): Int = items.indexOfFirst { it.id == id }

    companion object {
        val Empty = PlaybackQueue(emptyList(), -1)
    }
}

enum class QueueInsertion { Next, Last }

sealed interface EnqueueOutcome {
    data object NothingToAdd : EnqueueOutcome
    data class Enqueued(val count: Int, val insertion: QueueInsertion) : EnqueueOutcome
    data class StartedPlayback(val count: Int) : EnqueueOutcome
}
