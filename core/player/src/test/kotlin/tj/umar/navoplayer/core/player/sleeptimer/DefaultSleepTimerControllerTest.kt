package tj.umar.navoplayer.core.player.sleeptimer

import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.domain.model.SleepTimer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DefaultSleepTimerControllerTest {

    private fun TestScope.controller(): Pair<SleepTimerStore, DefaultSleepTimerController> {
        val clock = NavoClock { testScheduler.currentTime }
        val store = SleepTimerStore(clock).also { it.attach() }
        return store to DefaultSleepTimerController(store, clock)
    }

    @Test
    fun `starts off`() = runTest {
        val (_, controller) = controller()

        controller.observeSleepTimer().test {
            assertEquals(SleepTimer.Off, awaitItem())
        }
    }

    @Test
    fun `countdown ticks every second until zero`() = runTest {
        val (_, controller) = controller()
        controller.startCountdown(3.seconds)

        controller.observeSleepTimer().test {
            assertEquals(SleepTimer.Countdown(3_000, 3_000), awaitItem())
            assertEquals(SleepTimer.Countdown(2_000, 3_000), awaitItem())
            assertEquals(SleepTimer.Countdown(1_000, 3_000), awaitItem())
            assertEquals(SleepTimer.Countdown(0, 3_000), awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `unaligned countdown realigns to second boundary`() = runTest {
        val (_, controller) = controller()
        controller.startCountdown(2_500.milliseconds)

        controller.observeSleepTimer().test {
            assertEquals(2_500L, (awaitItem() as SleepTimer.Countdown).remainingMs)
            assertEquals(2_000L, (awaitItem() as SleepTimer.Countdown).remainingMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel stops countdown`() = runTest {
        val (store, controller) = controller()
        controller.startCountdown(60.seconds)

        controller.observeSleepTimer().test {
            awaitItem()
            controller.cancel()
            assertEquals(SleepTimer.Off, awaitItem())
            assertEquals(SleepTimerSchedule.Off, store.schedule.value)
        }
    }

    @Test
    fun `end of track emits once and switches from countdown`() = runTest {
        val (_, controller) = controller()
        controller.startCountdown(60.seconds)

        controller.observeSleepTimer().test {
            awaitItem()
            controller.startEndOfTrack()
            assertEquals(SleepTimer.EndOfTrack, awaitItem())
            expectNoEvents()
        }
    }
}
