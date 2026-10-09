package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor
import javax.inject.Inject

class ObserveNowPlayingUseCase @Inject constructor(
    private val monitor: NowPlayingMonitor,
) {
    operator fun invoke(): Flow<NowPlaying> = monitor.observeNowPlaying()
}
