package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.RemovedPlaylistTrack
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class RestorePlaylistTrackUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(removed: RemovedPlaylistTrack): NavoResult<Boolean> =
        navoRunCatching { playlistRepository.restoreTrack(removed) }
}
