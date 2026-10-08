package tj.umar.navoplayer.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import tj.umar.navoplayer.core.database.dao.FavoriteDao
import tj.umar.navoplayer.core.database.dao.PlaylistDao
import tj.umar.navoplayer.core.database.entity.FavoriteEntity
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity

const val NAVO_DATABASE_NAME = "navo.db"

@Database(
    entities = [PlaylistEntity::class, PlaylistTrackEntity::class, FavoriteEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class NavoDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    abstract fun favoriteDao(): FavoriteDao
}
