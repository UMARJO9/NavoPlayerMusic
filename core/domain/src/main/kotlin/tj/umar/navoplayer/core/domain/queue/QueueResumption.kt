package tj.umar.navoplayer.core.domain.queue

import tj.umar.navoplayer.core.domain.model.QueueItem
import tj.umar.navoplayer.core.domain.model.ResumableQueue
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.Track

fun SavedQueue.resolveAgainst(tracksById: Map<Long, Track>): ResumableQueue? {
    val newIndexOf = IntArray(items.size) { -1 }
    val kept = mutableListOf<QueueItem>()
    items.forEachIndexed { oldIndex, item ->
        val track = tracksById[item.trackId] ?: return@forEachIndexed
        newIndexOf[oldIndex] = kept.size
        kept += QueueItem(item.queueItemId, track)
    }
    if (kept.isEmpty()) return null

    val validOrder = shuffleOrder?.takeIf { it.isPermutationOf(items.size) }
    val remappedOrder = validOrder
        ?.mapNotNull { oldIndex -> newIndexOf[oldIndex].takeIf { it >= 0 } }
        ?.takeIf { it.isPermutationOf(kept.size) }

    val current = progress.currentIndex.takeIf { it in items.indices } ?: 0
    val playOrder = validOrder ?: items.indices.toList()
    val (startIndex, startPosition) = if (newIndexOf[current] >= 0) {
        val track = kept[newIndexOf[current]].track
        val position = progress.positionMs.coerceAtLeast(0)
        newIndexOf[current] to if (track.durationMs > 0 && position >= track.durationMs) 0L else position
    } else {
        val playPosition = playOrder.indexOf(current)
        val after = playOrder.drop(playPosition + 1).firstOrNull { newIndexOf[it] >= 0 }
        val before = playOrder.take(playPosition.coerceAtLeast(0)).lastOrNull { newIndexOf[it] >= 0 }
        newIndexOf[after ?: before ?: playOrder.first { newIndexOf[it] >= 0 }] to 0L
    }

    return ResumableQueue(
        items = kept,
        shuffleOrder = remappedOrder,
        startIndex = startIndex,
        startPositionMs = startPosition,
        shuffleEnabled = progress.shuffleEnabled,
        repeatMode = progress.repeatMode,
        source = source,
    )
}

private fun List<Int>.isPermutationOf(size: Int): Boolean =
    this.size == size && sorted() == (0 until size).toList()
