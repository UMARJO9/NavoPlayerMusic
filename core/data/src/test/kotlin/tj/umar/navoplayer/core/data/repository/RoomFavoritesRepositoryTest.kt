package tj.umar.navoplayer.core.data.repository

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
import tj.umar.navoplayer.core.testing.time.FakeNavoClock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomFavoritesRepositoryTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val clock = FakeNavoClock(now = 1_000)
    private val repository = RoomFavoritesRepository(database.favoriteDao(), clock)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `newest favorites come first`() = runTest {
        repository.addFavorite(1)
        clock.advanceBy(10)
        repository.addFavorite(2)

        assertEquals(listOf(2L, 1L), repository.observeFavoriteIds().first())
    }

    @Test
    fun `adding twice keeps first time`() = runTest {
        assertTrue(repository.addFavorite(1))
        clock.advanceBy(10)
        repository.addFavorite(2)
        clock.advanceBy(10)

        assertFalse(repository.addFavorite(1))
        assertEquals(listOf(2L, 1L), repository.observeFavoriteIds().first())
    }

    @Test
    fun `remove reports change`() = runTest {
        repository.addFavorite(1)

        assertTrue(repository.removeFavorite(1))
        assertFalse(repository.removeFavorite(1))
    }

    @Test
    fun `is favorite follows changes`() = runTest {
        repository.observeIsFavorite(3).test {
            assertFalse(awaitItem())
            repository.addFavorite(3)
            assertTrue(awaitItem())
            repository.removeFavorite(3)
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
