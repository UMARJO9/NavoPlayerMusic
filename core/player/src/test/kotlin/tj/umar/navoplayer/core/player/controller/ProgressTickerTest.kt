package tj.umar.navoplayer.core.player.controller

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTickerTest {

    @Test
    fun `emits immediately when paused trigger arrives`() = runTest {
        val triggers = MutableSharedFlow<Boolean>()

        progressTicks(triggers).test {
            triggers.emit(false)
            awaitItem()
            expectNoEvents()
        }
    }

    @Test
    fun `ticks every interval while playing`() = runTest {
        val triggers = MutableSharedFlow<Boolean>()

        progressTicks(triggers, intervalMs = 500).test {
            triggers.emit(true)
            awaitItem()
            val start = testScheduler.currentTime
            awaitItem()
            assertEquals(500L, testScheduler.currentTime - start)
            awaitItem()
            assertEquals(1_000L, testScheduler.currentTime - start)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stops ticking after pause`() = runTest {
        val triggers = MutableSharedFlow<Boolean>()

        progressTicks(triggers, intervalMs = 500).test {
            triggers.emit(true)
            awaitItem()
            triggers.emit(false)
            awaitItem()
            testScheduler.advanceTimeBy(5_000)
            expectNoEvents()
        }
    }

    @Test
    fun `re-emits for every trigger while paused`() = runTest {
        val triggers = MutableSharedFlow<Boolean>()

        progressTicks(triggers).test {
            triggers.emit(false)
            awaitItem()
            triggers.emit(false)
            awaitItem()
            expectNoEvents()
        }
    }
}
