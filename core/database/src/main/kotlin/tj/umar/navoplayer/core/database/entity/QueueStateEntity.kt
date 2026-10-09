package tj.umar.navoplayer.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val QUEUE_STATE_ROW_ID = 0

@Entity(tableName = "queue_state")
data class QueueStateEntity(
    @PrimaryKey val id: Int = QUEUE_STATE_ROW_ID,
    @ColumnInfo(name = "current_index") val currentIndex: Int,
    @ColumnInfo(name = "position_ms") val positionMs: Long,
    @ColumnInfo(name = "shuffle_enabled") val shuffleEnabled: Boolean,
    @ColumnInfo(name = "repeat_mode") val repeatMode: String,
    @ColumnInfo(name = "source_kind") val sourceKind: String?,
    @ColumnInfo(name = "source_playlist_id") val sourcePlaylistId: Long?,
    @ColumnInfo(name = "source_label") val sourceLabel: String?,
    @ColumnInfo(name = "saved_at") val savedAt: Long,
)
