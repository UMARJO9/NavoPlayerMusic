package tj.umar.navoplayer.core.domain.usecase

import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.navoRunCatching
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import javax.inject.Inject

class SetFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
) {
    suspend operator fun invoke(trackId: Long, favorite: Boolean): NavoResult<Boolean> = navoRunCatching {
        if (favorite) favoritesRepository.addFavorite(trackId) else favoritesRepository.removeFavorite(trackId)
    }
}
