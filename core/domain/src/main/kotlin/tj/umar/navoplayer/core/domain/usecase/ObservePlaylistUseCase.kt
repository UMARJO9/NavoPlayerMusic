package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.model.PlaylistDetail
import tj.umar.navoplayer.core.domain.playlist.toDetail
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class ObservePlaylistUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(id: Long): Flow<PlaylistDetail?> =
        combine(
            playlistRepository.observePlaylist(id),
            observeTracks.catalog(),
        ) { playlist, library -> playlist?.toDetail(library) }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
}
