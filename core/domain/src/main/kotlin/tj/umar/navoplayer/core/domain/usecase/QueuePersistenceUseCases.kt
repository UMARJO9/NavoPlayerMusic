package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.app.AudioAccessChecker
import tj.umar.navoplayer.core.domain.model.QueueResumeResult
import tj.umar.navoplayer.core.domain.model.SavedQueue
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.domain.queue.resolveAgainst
import tj.umar.navoplayer.core.domain.repository.PlaybackQueueRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import javax.inject.Inject

class LoadResumableQueueUseCase @Inject constructor(
    private val repository: PlaybackQueueRepository,
    private val trackRepository: TrackRepository,
    private val access: AudioAccessChecker,
) {
    suspend operator fun invoke(): QueueResumeResult {
        val result = navoRunCatching {
            val saved = repository.loadQueue() ?: return@navoRunCatching QueueResumeResult.NothingSaved
            if (!access.hasAudioAccess()) return@navoRunCatching QueueResumeResult.AccessDenied
            val tracks = trackRepository.getTracks(saved.items.map { it.trackId }.distinct())
            val resumable = saved.resolveAgainst(tracks.associateBy { it.id })
            if (resumable == null) {
                repository.clearQueue()
                QueueResumeResult.AllTracksMissing
            } else {
                QueueResumeResult.Resumable(resumable)
            }
        }
        return when (result) {
            is NavoResult.Success -> result.data
            is NavoResult.Error -> QueueResumeResult.Failed
        }
    }
}

class SavePlaybackQueueUseCase @Inject constructor(
    private val repository: PlaybackQueueRepository,
) {
    suspend operator fun invoke(queue: SavedQueue) = repository.saveQueue(queue)
}

class SavePlaybackProgressUseCase @Inject constructor(
    private val repository: PlaybackQueueRepository,
) {
    suspend operator fun invoke(progress: SavedQueueProgress) = repository.saveProgress(progress)
}

class ClearSavedPlaybackQueueUseCase @Inject constructor(
    private val repository: PlaybackQueueRepository,
) {
    suspend operator fun invoke() = repository.clearQueue()
}
