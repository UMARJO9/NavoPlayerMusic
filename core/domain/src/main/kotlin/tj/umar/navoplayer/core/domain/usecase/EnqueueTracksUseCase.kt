package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.EnqueueOutcome
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.QueueInsertion
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import javax.inject.Inject

class EnqueueTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(trackIds: List<Long>, insertion: QueueInsertion): NavoResult<EnqueueOutcome> =
        navoRunCatching {
            val distinctIds = trackIds.distinct()
            if (distinctIds.isEmpty()) return@navoRunCatching EnqueueOutcome.NothingToAdd
            val tracks = trackRepository.getTracks(distinctIds)
            if (tracks.isEmpty()) return@navoRunCatching EnqueueOutcome.NothingToAdd
            if (playbackController.observeQueue().first().items.isEmpty()) {
                playbackController.play(tracks, 0, PlaybackSource.Queue)
                EnqueueOutcome.StartedPlayback(tracks.size)
            } else {
                check(playbackController.enqueue(tracks, insertion)) { "Queue rejected tracks" }
                EnqueueOutcome.Enqueued(tracks.size, insertion)
            }
        }
}
