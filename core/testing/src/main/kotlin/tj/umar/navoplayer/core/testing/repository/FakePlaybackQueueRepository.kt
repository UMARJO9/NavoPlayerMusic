package tj.umar.navoplayer.core.testing.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.domain.repository.PlaybackQueueRepository

class FakePlaybackQueueRepository(initial: SavedQueue? = null) : PlaybackQueueRepository {

    private val saved = MutableStateFlow(initial)

    var loadError: Throwable? = null
    var writeError: Throwable? = null

    var saveCalls: Int = 0
        private set
    var progressCalls: Int = 0
        private set
    var clearCalls: Int = 0
        private set

    val current: SavedQueue?
        get() = saved.value

    override suspend fun loadQueue(): SavedQueue? {
        loadError?.let { throw it }
        return saved.value
    }

    override suspend fun saveQueue(queue: SavedQueue) {
        saveCalls++
        writeError?.let { throw it }
        saved.value = queue
    }

    override suspend fun saveProgress(progress: SavedQueueProgress) {
        progressCalls++
        writeError?.let { throw it }
        saved.value = saved.value?.copy(progress = progress)
    }

    override suspend fun clearQueue() {
        clearCalls++
        writeError?.let { throw it }
        saved.value = null
    }

    override fun observeSavedCurrentTrackId(): Flow<Long?> = saved.map { queue ->
        queue?.items?.getOrNull(queue.progress.currentIndex)?.trackId
    }
}
