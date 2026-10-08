package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class AddTracksToPlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: Long, trackIds: List<Long>): NavoResult<Int> {
        val distinctIds = trackIds.distinct()
        if (distinctIds.isEmpty()) return NavoResult.Success(0)
        return navoRunCatching { playlistRepository.addTracks(playlistId, distinctIds) }
    }
}
