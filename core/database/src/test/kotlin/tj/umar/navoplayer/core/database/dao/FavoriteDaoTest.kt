package tj.umar.navoplayer.core.database.dao

import androidx.room.Room
import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.database.NavoDatabase
import tj.umar.navoplayer.core.database.entity.FavoriteEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FavoriteDaoTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val dao = database.favoriteDao()

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `favorites are newest first then by id`() = runTest {
        dao.insert(FavoriteEntity(trackId = 1, addedAt = 10))
        dao.insert(FavoriteEntity(trackId = 2, addedAt = 30))
        dao.insert(FavoriteEntity(trackId = 3, addedAt = 10))

        assertEquals(listOf(2L, 3L, 1L), dao.observeFavoriteIds().first())
    }

    @Test
    fun `inserting existing favorite keeps original time`() = runTest {
        dao.insert(FavoriteEntity(trackId = 1, addedAt = 10))
        dao.insert(FavoriteEntity(trackId = 2, addedAt = 20))

        assertEquals(-1L, dao.insert(FavoriteEntity(trackId = 1, addedAt = 99)))
        assertEquals(listOf(2L, 1L), dao.observeFavoriteIds().first())
    }

    @Test
    fun `delete reports affected rows`() = runTest {
        dao.insert(FavoriteEntity(trackId = 1, addedAt = 10))

        assertEquals(1, dao.delete(1))
        assertEquals(0, dao.delete(1))
    }

    @Test
    fun `is favorite follows writes`() = runTest {
        dao.observeIsFavorite(5).test {
            assertFalse(awaitItem())
            dao.insert(FavoriteEntity(trackId = 5, addedAt = 1))
            assertTrue(awaitItem())
            dao.delete(5)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
