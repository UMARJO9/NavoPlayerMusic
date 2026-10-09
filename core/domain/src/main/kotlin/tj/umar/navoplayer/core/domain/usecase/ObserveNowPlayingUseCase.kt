package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.domain.playback.NowPlayingMonitor
import tj.umar.navoplayer.core.domain.repository.PlaybackQueueRepository
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import javax.inject.Inject

class ObserveNowPlayingUseCase @Inject constructor(
    private val monitor: NowPlayingMonitor,
    private val queueRepository: PlaybackQueueRepository,
    private val trackRepository: TrackRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<NowPlaying> = monitor.observeNowPlaying()
        .flatMapLatest { live ->
            if (live.track != null) flowOf(live) else savedNowPlaying()
        }
        .distinctUntilChanged()

    private fun savedNowPlaying(): Flow<NowPlaying> = queueRepository.observeSavedCurrentTrackId()
        .distinctUntilChanged()
        .map { trackId ->
            val track = trackId?.let { id ->
                runCatching { trackRepository.getTracks(listOf(id)).firstOrNull() }.getOrNull()
            }
            if (track != null) NowPlaying(track, isPlaying = false) else NowPlaying.Idle
        }
        .catch { emit(NowPlaying.Idle) }
}
