package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress

interface PlaybackQueueRepository {
    suspend fun loadQueue(): SavedQueue?
    suspend fun saveQueue(queue: SavedQueue)
    suspend fun saveProgress(progress: SavedQueueProgress)
    suspend fun clearQueue()
    fun observeSavedCurrentTrackId(): Flow<Long?>
}
