package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.InvalidPlaylistNameException
import tj.umar.navoplayer.core.domain.playlist.normalizePlaylistName
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class RenamePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) {
    suspend operator fun invoke(id: Long, name: String): NavoResult<Unit> {
        val normalized = normalizePlaylistName(name) ?: return NavoResult.Error(InvalidPlaylistNameException())
        return navoRunCatching {
            if (!playlistRepository.renamePlaylist(id, normalized)) throw NoSuchElementException("Playlist $id")
        }
    }
}
