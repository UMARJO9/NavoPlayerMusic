package tj.umar.navoplayer.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "track_id") val trackId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)
