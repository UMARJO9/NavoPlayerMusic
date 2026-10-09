package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.domain.repository.TrackRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    private val trackRepository: TrackRepository,
) {
    suspend operator fun invoke(ids: List<Long>): NavoResult<List<Track>> {
        val distinctIds = ids.distinct()
        if (distinctIds.isEmpty()) return NavoResult.Success(emptyList())
        return navoRunCatching { trackRepository.getTracks(distinctIds) }
    }
}
