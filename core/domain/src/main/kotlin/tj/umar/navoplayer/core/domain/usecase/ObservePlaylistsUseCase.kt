package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.PlaylistSummary
import tj.umar.navoplayer.core.domain.playlist.toSummary
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class ObservePlaylistsUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<PlaylistSummary>> =
        combine(
            playlistRepository.observePlaylists(),
            observeTracks.catalog(),
        ) { playlists, library -> playlists.map { it.toSummary(library) } }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
}
