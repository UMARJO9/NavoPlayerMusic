package tj.umar.navoplayer.core.data.repository

import androidx.room.Room
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
import tj.umar.navoplayer.core.testing.time.FakeNavoClock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomPlaylistRepositoryTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val clock = FakeNavoClock(now = 1_000)
    private val repository = RoomPlaylistRepository(database.playlistDao(), clock)

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun playlist(id: Long) = repository.observePlaylist(id).first()!!

    @Test
    fun `create stamps clock and keeps track order`() = runTest {
        val id = repository.createPlaylist("Mix", listOf(3, 1, 2))

        val playlist = playlist(id)
        assertEquals("Mix", playlist.name)
        assertEquals(1_000, playlist.createdAtMs)
        assertEquals(listOf(3L, 1L, 2L), playlist.trackIds)
    }

    @Test
    fun `add bumps update time only when tracks are added`() = runTest {
        val id = repository.createPlaylist("Mix", listOf(1))
        clock.advanceBy(10)

        assertEquals(0, repository.addTracks(id, listOf(1)))
        assertEquals(1_000, playlist(id).updatedAtMs)

        assertEquals(1, repository.addTracks(id, listOf(2)))
        assertEquals(1_010, playlist(id).updatedAtMs)
    }

    @Test
    fun `remove and rename bump update time`() = runTest {
        val id = repository.createPlaylist("Mix", listOf(1, 2))
        clock.advanceBy(5)

        assertTrue(repository.removeTrack(id, 1))
        assertEquals(1_005, playlist(id).updatedAtMs)
        assertFalse(repository.removeTrack(id, 1))

        clock.advanceBy(5)
        assertTrue(repository.renamePlaylist(id, "New"))
        assertEquals("New", playlist(id).name)
        assertEquals(1_010, playlist(id).updatedAtMs)
    }

    @Test
    fun `playlists are listed by latest update`() = runTest {
        val first = repository.createPlaylist("A", emptyList())
        clock.advanceBy(1)
        val second = repository.createPlaylist("B", emptyList())
        clock.advanceBy(1)
        repository.addTracks(first, listOf(9))

        assertEquals(listOf(first, second), repository.observePlaylists().first().map { it.id })
    }

    @Test
    fun `delete removes playlist`() = runTest {
        val id = repository.createPlaylist("Mix", listOf(1))

        assertTrue(repository.deletePlaylist(id))
        assertFalse(repository.deletePlaylist(id))
        assertNull(repository.observePlaylist(id).first())
    }
}
