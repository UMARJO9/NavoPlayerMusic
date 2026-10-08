package tj.umar.navoplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import tj.umar.navoplayer.core.database.dao.PlaylistDao
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity

const val NAVO_DATABASE_NAME = "navo.db"

@Database(
    entities = [PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NavoDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
}
