package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import tj.umar.navoplayer.core.common.dispatchers.IoDispatcher
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.data.mapper.storageValue
import tj.umar.navoplayer.core.data.mapper.toEntities
import tj.umar.navoplayer.core.data.mapper.toSavedQueue
import tj.umar.navoplayer.core.database.dao.PlaybackQueueDao
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.domain.repository.PlaybackQueueRepository
import javax.inject.Inject

internal class RoomPlaybackQueueRepository @Inject constructor(
    private val dao: PlaybackQueueDao,
    private val clock: NavoClock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PlaybackQueueRepository {

    override suspend fun loadQueue(): SavedQueue? = withContext(ioDispatcher) {
        val state = dao.state() ?: return@withContext null
        state.toSavedQueue(dao.items())
    }

    override suspend fun saveQueue(queue: SavedQueue) {
        withContext(ioDispatcher) {
            val (state, items) = queue.toEntities(clock.nowMillis())
            dao.replace(state, items)
        }
    }

    override suspend fun saveProgress(progress: SavedQueueProgress) {
        withContext(ioDispatcher) {
            dao.updateProgress(
                currentIndex = progress.currentIndex,
                positionMs = progress.positionMs,
                shuffleEnabled = progress.shuffleEnabled,
                repeatMode = progress.repeatMode.storageValue(),
                savedAt = clock.nowMillis(),
            )
        }
    }

    override suspend fun clearQueue() {
        withContext(ioDispatcher) { dao.clear() }
    }

    override fun observeSavedCurrentTrackId(): Flow<Long?> = dao.observeCurrentTrackId().distinctUntilChanged()
}
