package tj.umar.navoplayer.core.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MediaStoreTrackRepositoryTest {

    private val source = FakeAudioMediaSource()

    private fun TestScope.repository() =
        MediaStoreTrackRepository(source, StandardTestDispatcher(testScheduler))

    @Test
    fun `emits initial scan without waiting for debounce`() = runTest {
        source.rows = listOf(audioRow(1, "Song"))

        repository().observeTracks().test {
            assertEquals(listOf("Song"), awaitItem().map { it.title })
            assertEquals(0L, testScheduler.currentTime)
        }
    }

    @Test
    fun `rescans after change once debounce passes`() = runTest {
        source.rows = listOf(audioRow(1, "Song"))

        repository().observeTracks().test {
            awaitItem()
            source.awaitObserver()
            source.rows = listOf(audioRow(1, "Song"), audioRow(2, "New"))
            val changedAt = testScheduler.currentTime
            source.notifyChange()

            assertEquals(listOf("New", "Song"), awaitItem().map { it.title })
            assertTrue(testScheduler.currentTime - changedAt >= CHANGE_DEBOUNCE_MS)
        }
    }

    @Test
    fun `burst of changes triggers a single rescan`() = runTest {
        source.rows = listOf(audioRow(1, "Song"))

        repository().observeTracks().test {
            awaitItem()
            source.awaitObserver()
            source.rows = listOf(audioRow(2, "Other"))
            repeat(5) { source.notifyChange() }

            awaitItem()
            assertEquals(2, source.queryCount)
        }
    }

    @Test
    fun `identical rescan emits nothing`() = runTest {
        source.rows = listOf(audioRow(1, "Song"))

        repository().observeTracks().test {
            awaitItem()
            source.awaitObserver()
            source.notifyChange()
            testScheduler.advanceUntilIdle()

            assertEquals(2, source.queryCount)
            expectNoEvents()
        }
    }

    @Test
    fun `sorts by title ignoring case`() = runTest {
        source.rows = listOf(audioRow(1, "beta"), audioRow(2, "Alpha"))

        repository().observeTracks().test {
            assertEquals(listOf("Alpha", "beta"), awaitItem().map { it.title })
        }
    }

    @Test
    fun `empty source emits empty list`() = runTest {
        repository().observeTracks().test {
            assertEquals(emptyList<Any>(), awaitItem())
        }
    }
}
