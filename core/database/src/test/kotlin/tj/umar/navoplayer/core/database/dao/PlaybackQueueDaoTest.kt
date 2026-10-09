package tj.umar.navoplayer.core.database.dao

import androidx.room.Room
import app.cash.turbine.test
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
import tj.umar.navoplayer.core.database.entity.QueueItemEntity
import tj.umar.navoplayer.core.database.entity.QueueStateEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlaybackQueueDaoTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val dao = database.playbackQueueDao()

    @After
    fun tearDown() {
        database.close()
    }

    private fun state(current: Int = 0) = QueueStateEntity(
        currentIndex = current,
        positionMs = 1_000,
        shuffleEnabled = false,
        repeatMode = "off",
        sourceKind = null,
        sourcePlaylistId = null,
        sourceLabel = null,
        savedAt = 5,
    )

    private fun items(vararg trackIds: Long) = trackIds.mapIndexed { index, id ->
        QueueItemEntity(position = index, queueItemId = "q$index", trackId = id, shufflePosition = null)
    }

    @Test
    fun `replace stores ordered items and overwrites previous queue`() = runTest {
        dao.replace(state(), items(5, 6, 7))
        dao.replace(state(current = 1), items(8, 9))

        assertEquals(listOf(8L, 9L), dao.items().map { it.trackId })
        assertEquals(1, dao.state()?.currentIndex)
    }

    @Test
    fun `progress update changes state only when saved`() = runTest {
        assertEquals(0, dao.updateProgress(1, 2_000, true, "all", 9))

        dao.replace(state(), items(5, 6))
        assertEquals(1, dao.updateProgress(1, 2_000, true, "all", 9))

        val updated = dao.state()!!
        assertEquals(1, updated.currentIndex)
        assertEquals(2_000L, updated.positionMs)
        assertEquals("all", updated.repeatMode)
        assertEquals(2, dao.items().size)
    }

    @Test
    fun `clear empties queue`() = runTest {
        dao.replace(state(), items(5))

        dao.clear()

        assertNull(dao.state())
        assertTrue(dao.items().isEmpty())
    }

    @Test
    fun `current track follows queue changes`() = runTest {
        dao.observeCurrentTrackId().test {
            assertNull(awaitItem())
            dao.replace(state(), items(5, 6))
            assertEquals(5L, awaitItem())
            dao.updateProgress(1, 0, false, "off", 10)
            assertEquals(6L, awaitItem())
            dao.clear()
            assertNull(awaitItem())
        }
    }

    @Test
    fun `large queue round trips`() = runTest {
        val ids = (1L..5_000L).toList().toLongArray()

        dao.replace(state(current = 4_999), items(*ids))

        assertEquals(5_000, dao.items().size)
        dao.observeCurrentTrackId().test { assertEquals(5_000L, awaitItem()) }
    }
}
