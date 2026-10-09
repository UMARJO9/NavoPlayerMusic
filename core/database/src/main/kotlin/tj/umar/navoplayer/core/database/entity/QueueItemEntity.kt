package tj.umar.navoplayer.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val position: Int,
    @ColumnInfo(name = "queue_item_id") val queueItemId: String,
    @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "shuffle_position") val shufflePosition: Int?,
)
