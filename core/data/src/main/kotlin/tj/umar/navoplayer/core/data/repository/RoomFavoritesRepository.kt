package tj.umar.navoplayer.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.database.dao.FavoriteDao
import tj.umar.navoplayer.core.database.entity.FavoriteEntity
import tj.umar.navoplayer.core.domain.repository.FavoritesRepository
import javax.inject.Inject

private const val ALREADY_PRESENT = -1L

internal class RoomFavoritesRepository @Inject constructor(
    private val dao: FavoriteDao,
    private val clock: NavoClock,
) : FavoritesRepository {

    override fun observeFavoriteIds(): Flow<List<Long>> = dao.observeFavoriteIds().distinctUntilChanged()

    override fun observeIsFavorite(trackId: Long): Flow<Boolean> = dao.observeIsFavorite(trackId).distinctUntilChanged()

    override suspend fun addFavorite(trackId: Long): Boolean =
        dao.insert(FavoriteEntity(trackId = trackId, addedAt = clock.nowMillis())) != ALREADY_PRESENT

    override suspend fun removeFavorite(trackId: Long): Boolean = dao.delete(trackId) > 0
}
