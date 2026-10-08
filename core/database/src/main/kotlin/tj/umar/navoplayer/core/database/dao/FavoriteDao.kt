package tj.umar.navoplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT track_id FROM favorites ORDER BY added_at DESC, track_id DESC")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE track_id = :trackId)")
    fun observeIsFavorite(trackId: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteEntity): Long

    @Query("DELETE FROM favorites WHERE track_id = :trackId")
    suspend fun delete(trackId: Long): Int
}
