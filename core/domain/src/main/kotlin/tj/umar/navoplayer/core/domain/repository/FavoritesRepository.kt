package tj.umar.navoplayer.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.RemovedFavorite

interface FavoritesRepository {

    fun observeFavoriteIds(): Flow<List<Long>>

    fun observeIsFavorite(trackId: Long): Flow<Boolean>

    suspend fun addFavorite(trackId: Long): Boolean

    suspend fun removeFavorite(trackId: Long): RemovedFavorite?

    suspend fun restoreFavorite(removed: RemovedFavorite): Boolean
}
