package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.first
import tj.umar.navoplayer.core.domain.model.TransportCommand
import tj.umar.navoplayer.core.domain.model.TransportOutcome
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor
import tj.umar.navoplayer.core.domain.playback.PlaybackController
import javax.inject.Inject

class SendTransportCommandUseCase @Inject constructor(
    private val monitor: NowPlayingMonitor,
    private val playbackController: PlaybackController,
) {
    suspend operator fun invoke(command: TransportCommand): TransportOutcome {
        if (monitor.observeNowPlaying().first().track == null) return TransportOutcome.NoActiveSession
        when (command) {
            TransportCommand.TogglePlayPause -> playbackController.togglePlayPause()
            TransportCommand.SkipToNext -> playbackController.skipToNext()
            TransportCommand.SkipToPrevious -> playbackController.skipToPrevious()
        }
        return TransportOutcome.Sent
    }
}
