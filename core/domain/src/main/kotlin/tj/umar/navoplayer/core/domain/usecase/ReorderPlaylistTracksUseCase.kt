package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class ReorderPlaylistTracksUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(playlistId: Long, orderedTrackIds: List<Long>): NavoResult<Boolean> =
        navoRunCatching { playlistRepository.reorderTracks(playlistId, orderedTrackIds) }
}
