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

    @Test
    fun `changes during a slow scan do not cancel it`() = runTest {
        source.rows = listOf(audioRow(1, "Song"))
        source.queryDelayMs = 1_000

        repository().observeTracks().test {
            source.awaitObserver()
            repeat(3) {
                testScheduler.advanceTimeBy(CHANGE_DEBOUNCE_MS + 100)
                source.notifyChange()
            }

            assertEquals(listOf("Song"), awaitItem().map { it.title })
            assertTrue(testScheduler.currentTime < 2_000)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `get tracks keeps requested order and drops missing ids`() = runTest {
        source.rows = listOf(audioRow(1, "One"), audioRow(2, "Two"), audioRow(3, "Three"))

        val tracks = repository().getTracks(listOf(3, 9, 1))

        assertEquals(listOf("Three", "One"), tracks.map { it.title })
        assertEquals(0, source.queryCount)
    }

    @Test
    fun `get tracks without ids skips query`() = runTest {
        assertTrue(repository().getTracks(emptyList()).isEmpty())
        assertEquals(0, source.idQueryCount)
    }
}
