package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.favorite.toFavoritesSummary
import tj.umar.navoplayer.core.domain.model.PlaylistsOverview
import tj.umar.navoplayer.core.domain.playlist.toSummary
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import tj.umar.navoplayer.core.domain.repository.PlaylistRepository
import javax.inject.Inject

class ObservePlaylistsOverviewUseCase @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<PlaylistsOverview> =
        combine(
            playlistRepository.observePlaylists(),
            favoritesRepository.observeFavoriteIds(),
            observeTracks.catalog(),
        ) { playlists, favoriteIds, library ->
            PlaylistsOverview(
                favorites = favoriteIds.toFavoritesSummary(library),
                playlists = playlists.map { it.toSummary(library) },
            )
        }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
}
