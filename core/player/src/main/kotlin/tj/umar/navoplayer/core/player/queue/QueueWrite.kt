package tj.umar.navoplayer.core.player.queue

import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress

internal sealed interface QueueWrite {
    data class Full(val queue: SavedQueue) : QueueWrite
    data class Progress(val progress: SavedQueueProgress) : QueueWrite
    data object Clear : QueueWrite
}

internal fun QueueWrite?.mergedWith(next: QueueWrite): QueueWrite = when (next) {
    is QueueWrite.Full, QueueWrite.Clear -> next
    is QueueWrite.Progress -> when (this) {
        is QueueWrite.Full -> copy(queue = queue.copy(progress = next.progress))
        QueueWrite.Clear -> QueueWrite.Clear
        is QueueWrite.Progress, null -> next
    }
}
