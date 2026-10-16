package tj.umar.navoplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.database.entity.FavoriteEntity

@Dao
abstract class FavoriteDao {

    @Query("SELECT track_id FROM favorites ORDER BY added_at DESC, track_id DESC")
    abstract fun observeFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE track_id = :trackId)")
    abstract fun observeIsFavorite(trackId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(entity: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE track_id = :trackId")
    abstract suspend fun delete(trackId: Long): Int

    @Query("SELECT * FROM favorites WHERE track_id = :trackId")
    abstract suspend fun find(trackId: Long): FavoriteEntity?

    @Transaction
    open suspend fun removeReturning(trackId: Long): FavoriteEntity? {
        val existing = find(trackId) ?: return null
        delete(trackId)
        return existing
    }
}
