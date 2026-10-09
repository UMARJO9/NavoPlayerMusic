package tj.umar.navoplayer.core.player.sleeptimer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.testing.time.FakeNavoClock

class SleepTimerStoreTest {

    private val clock = FakeNavoClock(now = 1_000)
    private val store = SleepTimerStore(clock)

    @Test
    fun `detached store rejects timers`() {
        assertFalse(store.startCountdown(60_000))
        assertFalse(store.startEndOfTrack())
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `countdown ends after duration`() {
        store.attach()

        assertTrue(store.startCountdown(60_000))

        val schedule = store.schedule.value as SleepTimerSchedule.Countdown
        assertEquals(61_000, schedule.endsAtMs)
        assertEquals(60_000, schedule.durationMs)
    }

    @Test
    fun `restarting same timer creates new schedule`() {
        store.attach()
        store.startCountdown(60_000)
        val first = store.schedule.value

        store.startCountdown(60_000)

        assertNotEquals(first, store.schedule.value)
    }

    @Test
    fun `end of track and cancel`() {
        store.attach()

        assertTrue(store.startEndOfTrack())
        assertTrue(store.schedule.value is SleepTimerSchedule.EndOfTrack)

        store.cancel()
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `complete ignores stale schedule`() {
        store.attach()
        store.startCountdown(60_000)
        val stale = store.schedule.value
        store.startEndOfTrack()

        store.complete(stale)

        assertTrue(store.schedule.value is SleepTimerSchedule.EndOfTrack)
    }

    @Test
    fun `complete clears current schedule`() {
        store.attach()
        store.startEndOfTrack()

        store.complete(store.schedule.value)

        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `detach clears and rejects`() {
        store.attach()
        store.startCountdown(60_000)

        store.detach()

        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
        assertFalse(store.startCountdown(60_000))
    }
}
