package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import javax.inject.Inject

class ObserveTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
) {
    operator fun invoke(): Flow<List<Track>> = trackRepository.observeTracks()
}
