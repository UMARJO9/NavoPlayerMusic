package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import tj.umar.navoplayer.core.common.dispatchers.DefaultDispatcher
import tj.umar.navoplayer.core.domain.favorite.toFavoriteTracks
import tj.umar.navoplayer.core.domain.model.FavoriteTracks
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import javax.inject.Inject

class ObserveFavoriteTracksUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val observeTracks: ObserveTracksUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<FavoriteTracks> =
        combine(
            favoritesRepository.observeFavoriteIds(),
            observeTracks.catalog(),
        ) { ids, library -> ids.toFavoriteTracks(library) }
            .flowOn(defaultDispatcher)
            .distinctUntilChanged()
}
