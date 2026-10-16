package tj.umar.navoplayer.core.database.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.database.NavoDatabase
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity

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

    private suspend fun playlistWithPositions(vararg rows: Pair<Long, Int>): Long {
        val id = dao.insertPlaylist(PlaylistEntity(name = "Mix", createdAt = 100, updatedAt = 100))
        dao.insertTracks(rows.map { (trackId, position) -> PlaylistTrackEntity(id, trackId, position) })
        return id
    }

    @Test
    fun `remove at returns index of middle track and compacts positions`() = runTest {
        val id = playlistWithPositions(1L to 0, 2L to 3, 3L to 7)

        val index = dao.removeTrackAt(id, 2, updatedAt = 200)

        assertEquals(1, index)
        assertEquals(listOf(1L to 0, 3L to 1), positions(id))
        assertEquals(200L, updatedAt(id))
    }

    @Test
    fun `remove at of missing track returns null without touching`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)

        assertNull(dao.removeTrackAt(id, 9, updatedAt = 200))
        assertEquals(100L, updatedAt(id))
        assertEquals(listOf(1L to 0, 2L to 1), positions(id))
    }

    @Test
    fun `restore puts track back at original index`() = runTest {
        val id = playlistWithPositions(1L to 0, 2L to 4, 3L to 9, 4L to 12)
        val index = dao.removeTrackAt(id, 3, updatedAt = 200)!!

        val restored = dao.restoreTrack(id, 3, index, updatedAt = 300)

        assertTrue(restored)
        assertEquals(listOf(1L to 0, 2L to 1, 3L to 2, 4L to 3), positions(id))
        assertEquals(300L, updatedAt(id))
    }

    @Test
    fun `restore into playlist with gaps rewrites dense ranks`() = runTest {
        val id = playlistWithPositions(1L to 2, 2L to 5)

        assertTrue(dao.restoreTrack(id, 7, 1, updatedAt = 300))

        assertEquals(listOf(1L to 0, 7L to 1, 2L to 2), positions(id))
    }

    @Test
    fun `restore clamps index past end and negative`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)

        assertTrue(dao.restoreTrack(id, 3, 10, updatedAt = 200))
        assertTrue(dao.restoreTrack(id, 4, -3, updatedAt = 300))

        assertEquals(listOf(4L to 0, 1L to 1, 2L to 2, 3L to 3), positions(id))
    }

    @Test
    fun `restore of present track returns false without touching`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)

        assertFalse(dao.restoreTrack(id, 2, 0, updatedAt = 200))
        assertEquals(listOf(1L to 0, 2L to 1), positions(id))
        assertEquals(100L, updatedAt(id))
    }

    @Test
    fun `restore into deleted playlist returns false`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2), now = 100)
        dao.delete(id)

        assertFalse(dao.restoreTrack(id, 3, 0, updatedAt = 200))
        assertTrue(dao.trackIds(id).isEmpty())
    }

    @Test
    fun `full reorder writes dense positions without touching`() = runTest {
        val id = playlistWithPositions(1L to 0, 2L to 3, 3L to 8)

        assertTrue(dao.reorderTracks(id, listOf(3, 1, 2)))

        assertEquals(listOf(3L to 0, 1L to 1, 2L to 2), positions(id))
        assertEquals(100L, updatedAt(id))
    }

    @Test
    fun `subset reorder keeps slots of other tracks`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2, 3, 4, 5), now = 100)

        assertTrue(dao.reorderTracks(id, listOf(5, 3, 1)))

        assertEquals(listOf(5L, 2L, 3L, 4L, 1L), dao.orderedTrackIds(id))
    }

    @Test
    fun `reorder filters unknown and duplicate ids`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2, 3), now = 100)

        assertTrue(dao.reorderTracks(id, listOf(3, 9, 3, 1, 2, 1)))

        assertEquals(listOf(3L, 1L, 2L), dao.orderedTrackIds(id))
    }

    @Test
    fun `reorder in current order changes nothing`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2, 3), now = 100)

        assertTrue(dao.reorderTracks(id, listOf(1, 2, 3)))

        assertEquals(listOf(1L to 0, 2L to 1, 3L to 2), positions(id))
        assertEquals(100L, updatedAt(id))
    }

    @Test
    fun `reorder of missing playlist returns false`() = runTest {
        assertFalse(dao.reorderTracks(42, listOf(1, 2)))
    }

    @Test
    fun `reorder emits final order once`() = runTest {
        val id = dao.createWithTracks("Mix", listOf(1, 2, 3, 4), now = 100)

        dao.observePlaylist(id).test {
            awaitItem()
            dao.reorderTracks(id, listOf(4, 3, 2, 1))
            val reordered = awaitItem()!!.tracks.sortedBy { it.position }.map { it.trackId }
            assertEquals(listOf(4L, 3L, 2L, 1L), reordered)
            dao.appendTracks(id, listOf(5), updatedAt = 200)
            val appended = awaitItem()!!.tracks.sortedBy { it.position }.map { it.trackId }
            assertEquals(listOf(4L, 3L, 2L, 1L, 5L), appended)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
