package tj.umar.navoplayer.core.data.repository

import androidx.room.Room
import app.cash.turbine.test
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.database.NavoDatabase
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SavedQueueProgress
import tj.umar.navoplayer.core.testing.data.TestSavedQueues
import tj.umar.navoplayer.core.testing.data.TestTracks
import tj.umar.navoplayer.core.testing.time.FakeNavoClock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomPlaybackQueueRepositoryTest {

    private val database = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), NavoDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    private fun TestScope.repository() =
        RoomPlaybackQueueRepository(database.playbackQueueDao(), FakeNavoClock(now = 100), StandardTestDispatcher(testScheduler))

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saved queue loads back`() = runTest {
        val repository = repository()

        repository.saveQueue(TestSavedQueues.alphaBeta)

        assertEquals(TestSavedQueues.alphaBeta, repository.loadQueue())
    }

    @Test
    fun `progress before any queue is ignored`() = runTest {
        val repository = repository()

        repository.saveProgress(SavedQueueProgress(0, 1_000, false, RepeatMode.All))

        assertNull(repository.loadQueue())
    }

    @Test
    fun `progress updates only progress`() = runTest {
        val repository = repository()
        repository.saveQueue(TestSavedQueues.alphaBeta)
        val progress = SavedQueueProgress(0, 44_000, shuffleEnabled = false, repeatMode = RepeatMode.One)

        repository.saveProgress(progress)

        assertEquals(TestSavedQueues.alphaBeta.copy(progress = progress), repository.loadQueue())
    }

    @Test
    fun `clear removes queue`() = runTest {
        val repository = repository()
        repository.saveQueue(TestSavedQueues.alphaBeta)

        repository.clearQueue()

        assertNull(repository.loadQueue())
    }

    @Test
    fun `current track id emits distinct values`() = runTest {
        val repository = repository()

        repository.observeSavedCurrentTrackId().test {
            assertNull(awaitItem())
            repository.saveQueue(TestSavedQueues.alphaBeta)
            assertEquals(TestTracks.beta.id, awaitItem())
            repository.saveProgress(TestSavedQueues.alphaBeta.progress.copy(positionMs = 99_000))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
