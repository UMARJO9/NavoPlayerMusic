package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.model.RemovedFavorite
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import javax.inject.Inject

class RestoreFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(removed: RemovedFavorite): NavoResult<Boolean> =
        navoRunCatching { favoritesRepository.restoreFavorite(removed) }
}
