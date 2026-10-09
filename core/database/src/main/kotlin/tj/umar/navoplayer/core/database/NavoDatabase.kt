package tj.umar.navoplayer.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import tj.umar.navoplayer.core.database.dao.FavoriteDao
import tj.umar.navoplayer.core.database.dao.PlaybackQueueDao
import tj.umar.navoplayer.core.database.dao.PlaylistDao
import tj.umar.navoplayer.core.database.entity.FavoriteEntity
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity
import tj.umar.navoplayer.core.database.entity.QueueItemEntity
import tj.umar.navoplayer.core.database.entity.QueueStateEntity

const val NAVO_DATABASE_NAME = "navo.db"

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        FavoriteEntity::class,
        QueueStateEntity::class,
        QueueItemEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)],
)
abstract class NavoDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    abstract fun favoriteDao(): FavoriteDao

    abstract fun playbackQueueDao(): PlaybackQueueDao
}
