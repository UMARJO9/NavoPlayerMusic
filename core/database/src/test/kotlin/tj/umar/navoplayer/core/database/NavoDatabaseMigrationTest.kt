package tj.umar.navoplayer.core.database

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.database.entity.FavoriteEntity
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class NavoDatabaseMigrationTest {

    private val context = RuntimeEnvironment.getApplication()
    private val databaseFile = context.getDatabasePath("migration-test.db")
    private var database: NavoDatabase? = null

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(databaseFile.name)
    }

    private fun createVersionOne() {
        val schema = JSONObject(File("schemas/tj.umar.navoplayer.core.database.NavoDatabase/1.json").readText())
            .getJSONObject("database")
        databaseFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                db.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: continue
                for (j in 0 until indices.length()) {
                    db.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) db.execSQL(setup.getString(i))
            db.execSQL("INSERT INTO playlists (id, name, created_at, updated_at) VALUES (1, 'Mix', 10, 20)")
            db.execSQL("INSERT INTO playlist_tracks (playlist_id, track_id, position) VALUES (1, 7, 0), (1, 8, 1)")
            db.version = 1
        }
    }

    @Test
    fun `migration from 1 to 2 keeps playlists and adds favorites`() = runTest {
        createVersionOne()

        val migrated = Room.databaseBuilder(context, NavoDatabase::class.java, databaseFile.absolutePath)
            .allowMainThreadQueries()
            .build()
            .also { database = it }

        val playlist = migrated.playlistDao().observePlaylist(1).first()!!
        assertEquals("Mix", playlist.playlist.name)
        assertEquals(listOf(7L, 8L), playlist.tracks.sortedBy { it.position }.map { it.trackId })
        assertTrue(migrated.favoriteDao().observeFavoriteIds().first().isEmpty())

        migrated.favoriteDao().insert(FavoriteEntity(trackId = 7, addedAt = 30))
        assertEquals(listOf(7L), migrated.favoriteDao().observeFavoriteIds().first())
        assertEquals(2, migrated.openHelper.readableDatabase.version)
    }
}
