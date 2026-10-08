package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.InvalidPlaylistNameException
import tj.umar.navoplayer.core.domain.playlist.normalizePlaylistName
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(name: String, trackIds: List<Long> = emptyList()): NavoResult<Long> {
        val normalized = normalizePlaylistName(name) ?: return NavoResult.Error(InvalidPlaylistNameException())
        return navoRunCatching { playlistRepository.createPlaylist(normalized, trackIds.distinct()) }
    }
}
