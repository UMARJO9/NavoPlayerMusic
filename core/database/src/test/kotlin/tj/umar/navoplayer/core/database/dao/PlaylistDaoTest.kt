package tj.umar.navoplayer.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.database.NavoDatabase
import tj.umar.navoplayer.core.database.entity.PlaylistEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaylistDaoTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val dao = database.playlistDao()

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun positions(playlistId: Long): List<Pair<Long, Int>> =
        dao.observePlaylist(playlistId).first()!!.tracks.sortedBy { it.position }.map { it.trackId to it.position }

    private suspend fun updatedAt(playlistId: Long): Long = dao.observePlaylist(playlistId).first()!!.playlist.updatedAt

    @Test
    fun `playlists are ordered by update time then id`() = runTest {
        val first = dao.insertPlaylist(PlaylistEntity(name = "A", createdAt = 1, updatedAt = 5))
        val second = dao.insertPlaylist(PlaylistEntity(name = "B", createdAt = 2, updatedAt = 9))
        val third = dao.insertPlaylist(PlaylistEntity(name = "C", createdAt = 3, updatedAt = 5))

        val ids = dao.observePlaylists().first().map { it.playlist.id }

        assertEquals(listOf(second, third, first), ids)
    }

    @Test
    fun `create with tracks stores distinct tracks in order`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(30, 10, 30, 20), now = 100)

        assertEquals(listOf(30L to 0, 10L to 1, 20L to 2), positions(id))
        assertEquals(100L, updatedAt(id))
    }

    @Test
    fun `append skips existing and duplicate ids and continues positions`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)

        val added = dao.appendTracks(id, listOf(2, 3, 3, 4), updatedAt = 200)

        assertEquals(2, added)
        assertEquals(listOf(1L to 0, 2L to 1, 3L to 2, 4L to 3), positions(id))
        assertEquals(200L, updatedAt(id))
    }

    @Test
    fun `append of present tracks does not touch playlist`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1), now = 100)

        val added = dao.appendTracks(id, listOf(1), updatedAt = 200)

        assertEquals(0, added)
        assertEquals(100L, updatedAt(id))
    }

    @Test
    fun `append after removal uses next position`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)
        dao.deleteTrack(id, 2)

        dao.appendTracks(id, listOf(3), updatedAt = 200)

        assertEquals(listOf(1L to 0, 3L to 1), positions(id))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun `append to missing playlist fails`() = runTest {
        dao.appendTracks(42, listOf(1), updatedAt = 200)
    }

    @Test
    fun `rename reports affected rows`() = runTest {
        val id = dao.createWithTracks("Old", emptyList(), now = 100)

        assertEquals(1, dao.rename(id, "New", updatedAt = 300))
        assertEquals(0, dao.rename(999, "New", updatedAt = 300))
        assertEquals("New", dao.observePlaylist(id).first()!!.playlist.name)
    }

    @Test
    fun `delete cascades tracks`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)

        assertEquals(1, dao.delete(id))

        assertNull(dao.observePlaylist(id).first())
        assertTrue(dao.trackIds(id).isEmpty())
    }

    @Test
    fun `observe emits after writes`() = runTest {
        val id = dao.createWithTracks("Mix", emptyList(), now = 100)

        dao.observePlaylist(id).test {
            assertTrue(awaitItem()!!.tracks.isEmpty())
            dao.appendTracks(id, listOf(7), updatedAt = 200)
            assertEquals(listOf(7L), awaitItem()!!.tracks.map { it.trackId })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
