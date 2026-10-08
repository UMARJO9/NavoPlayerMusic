package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import javax.inject.Inject

class ObserveIsFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    operator fun invoke(trackId: Long): Flow<Boolean> = favoritesRepository.observeIsFavorite(trackId)
}
