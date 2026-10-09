package tj.umar.navoplayer.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.database.entity.QueueItemEntity
import tj.umar.navoplayer.core.database.entity.QueueStateEntity

@Dao
abstract class PlaybackQueueDao {

    @Query("SELECT * FROM queue_state WHERE id = 0")
    abstract suspend fun state(): QueueStateEntity?

    @Query("SELECT * FROM queue_items ORDER BY position")
    abstract suspend fun items(): List<QueueItemEntity>

    @Query(
        "SELECT qi.track_id FROM queue_items qi " +
            "JOIN queue_state qs ON qs.id = 0 AND qi.position = qs.current_index",
    )
    abstract fun observeCurrentTrackId(): Flow<Long?>

    @Upsert
    abstract suspend fun upsertState(state: QueueStateEntity)

    @Insert
    abstract suspend fun insertItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items")
    abstract suspend fun deleteItems()

    @Query("DELETE FROM queue_state")
    abstract suspend fun deleteState()

    @Query(
        "UPDATE queue_state SET current_index = :currentIndex, position_ms = :positionMs, " +
            "shuffle_enabled = :shuffleEnabled, repeat_mode = :repeatMode, saved_at = :savedAt WHERE id = 0",
    )
    abstract suspend fun updateProgress(
        currentIndex: Int,
        positionMs: Long,
        shuffleEnabled: Boolean,
        repeatMode: String,
        savedAt: Long,
    ): Int

    @Transaction
    open suspend fun replace(state: QueueStateEntity, items: List<QueueItemEntity>) {
        deleteItems()
        insertItems(items)
        upsertState(state)
    }

    @Transaction
    open suspend fun clear() {
        deleteItems()
        deleteState()
    }
}
