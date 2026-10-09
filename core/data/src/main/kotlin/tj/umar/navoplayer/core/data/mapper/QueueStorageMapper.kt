package tj.umar.navoplayer.core.data.mapper

import tj.umar.navoplayer.core.database.entity.QueueItemEntity
import tj.umar.navoplayer.core.database.entity.QueueStateEntity
import tj.umar.navoplayer.core.domain.model.QueueItemId
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueItem
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress

internal fun RepeatMode.storageValue(): String = when (this) {
    RepeatMode.Off -> "off"
    RepeatMode.All -> "all"
    RepeatMode.One -> "one"
}

internal fun repeatModeOf(value: String?): RepeatMode =
    RepeatMode.entries.firstOrNull { it.storageValue() == value } ?: RepeatMode.Off

internal fun SavedQueue.toEntities(savedAt: Long): Pair<QueueStateEntity, List<QueueItemEntity>> {
    val shufflePositions = shuffleOrder?.let { order ->
        IntArray(items.size).also { positions -> order.forEachIndexed { position, index -> positions[index] = position } }
    }
    val stored = source.toStored()
    val state = QueueStateEntity(
        currentIndex = progress.currentIndex,
        positionMs = progress.positionMs,
        shuffleEnabled = progress.shuffleEnabled,
        repeatMode = progress.repeatMode.storageValue(),
        sourceKind = stored.kind,
        sourcePlaylistId = stored.playlistId,
        sourceLabel = stored.label,
        savedAt = savedAt,
    )
    val rows = items.mapIndexed { index, item ->
        QueueItemEntity(
            position = index,
            queueItemId = item.queueItemId.value,
            trackId = item.trackId,
            shufflePosition = shufflePositions?.get(index),
        )
    }
    return state to rows
}

internal fun QueueStateEntity.toSavedQueue(items: List<QueueItemEntity>): SavedQueue? {
    if (items.isEmpty()) return null
    val ordered = items.sortedBy { it.position }
    val shuffleOrder = if (shuffleEnabled) ordered.shuffleOrder() else null
    return SavedQueue(
        items = ordered.map { SavedQueueItem(QueueItemId(it.queueItemId), it.trackId) },
        shuffleOrder = shuffleOrder,
        progress = SavedQueueProgress(
            currentIndex = currentIndex.coerceIn(0, ordered.lastIndex),
            positionMs = positionMs.coerceAtLeast(0),
            shuffleEnabled = shuffleEnabled,
            repeatMode = repeatModeOf(repeatMode),
        ),
        source = StoredPlaybackSource(sourceKind, sourcePlaylistId, sourceLabel).toPlaybackSource(),
    )
}

private fun List<QueueItemEntity>.shuffleOrder(): List<Int>? {
    val positions = map { it.shufflePosition ?: return null }
    if (positions.sorted() != indices.toList()) return null
    return indices.sortedBy { positions[it] }
}
