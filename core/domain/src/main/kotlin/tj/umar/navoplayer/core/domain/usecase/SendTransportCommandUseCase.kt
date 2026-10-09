package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.model.TransportOutcome
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import tj.umar.navoplayer.core.domain.repository.PlaybackQueueRepository
import javax.inject.Inject

class SendTransportCommandUseCase @Inject constructor(
    private val monitor: NowPlayingMonitor,
    private val queueRepository: PlaybackQueueRepository,
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(command: TransportCommand): TransportOutcome {
        val active = monitor.observeNowPlaying().first().track != null
        if (!active && !(command == TransportCommand.TogglePlayPause && hasSavedQueue())) {
            return TransportOutcome.NoActiveSession
        }
        when (command) {
            TransportCommand.TogglePlayPause -> playbackController.togglePlayPause()
            TransportCommand.SkipToNext -> playbackController.skipToNext()
            TransportCommand.SkipToPrevious -> playbackController.skipToPrevious()
        }
        return TransportOutcome.Sent
    }

    private suspend fun hasSavedQueue(): Boolean =
        runCatching { queueRepository.observeSavedCurrentTrackId().first() != null }.getOrDefault(false)
}
