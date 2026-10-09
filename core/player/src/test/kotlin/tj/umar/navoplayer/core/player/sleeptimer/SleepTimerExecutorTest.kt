package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.common.time.NavoClock

private class FakeSleepTimerPlayer : SleepTimerPlayer {
    override var isPlaying: Boolean = true
    override var volume: Float = 0.8f
        set(value) {
            field = value
            volumeHistory += value
        }
    val volumeHistory = mutableListOf<Float>()
    var pauseCalls = 0
    var pauseAtEnd = false
    override val events = MutableSharedFlow<SleepTimerPlayerEvent>(extraBufferCapacity = 16)

    override fun pause() {
        pauseCalls++
        isPlaying = false
    }

    override fun setPauseAtEndOfMediaItems(enabled: Boolean) {
        pauseAtEnd = enabled
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SleepTimerExecutorTest {

    private val player = FakeSleepTimerPlayer()
    private var sleptMs = 0L

    private fun TestScope.startExecutor(): Pair<SleepTimerStore, SleepTimerExecutor> {
        val clock = NavoClock { testScheduler.currentTime + sleptMs }
        val store = SleepTimerStore(clock).also { it.attach() }
        val executor = SleepTimerExecutor(store, player, clock, fadeMs = 10_000)
        executor.start(backgroundScope)
        runCurrent()
        return store to executor
    }

    @Test
    fun `countdown fades out then pauses and restores volume`() = runTest {
        val (store, _) = startExecutor()
        store.startCountdown(60_000)

        advanceTimeBy(49_000)
        runCurrent()
        assertEquals(0.8f, player.volume)
        assertEquals(0, player.pauseCalls)

        advanceTimeBy(5_000)
        runCurrent()
        assertTrue(player.volume < 0.8f)
        assertEquals(0, player.pauseCalls)

        advanceTimeBy(6_100)
        runCurrent()
        assertEquals(1, player.pauseCalls)
        assertEquals(0.8f, player.volume)
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
        val fade = player.volumeHistory.dropLast(1)
        assertEquals(fade.sortedDescending(), fade)
    }

    @Test
    fun `paused at deadline completes without pausing`() = runTest {
        val (store, _) = startExecutor()
        player.isPlaying = false
        store.startCountdown(30_000)

        advanceTimeBy(31_000)
        runCurrent()

        assertEquals(0, player.pauseCalls)
        assertTrue(player.volumeHistory.isEmpty())
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `resume before deadline fades remaining time`() = runTest {
        val (store, _) = startExecutor()
        player.isPlaying = false
        store.startCountdown(30_000)

        advanceTimeBy(25_000)
        runCurrent()
        player.isPlaying = true
        player.events.emit(SleepTimerPlayerEvent.PlayingChanged(true))
        runCurrent()
        advanceTimeBy(6_000)
        runCurrent()

        assertEquals(1, player.pauseCalls)
        assertEquals(0.8f, player.volume)
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `cancel during fade restores volume without pausing`() = runTest {
        val (store, _) = startExecutor()
        store.startCountdown(20_000)

        advanceTimeBy(15_000)
        runCurrent()
        store.cancel()
        runCurrent()

        assertEquals(0.8f, player.volume)
        assertEquals(0, player.pauseCalls)
    }

    @Test
    fun `end of track pauses at item end`() = runTest {
        val (store, _) = startExecutor()
        store.startEndOfTrack()
        runCurrent()
        assertTrue(player.pauseAtEnd)

        player.events.emit(SleepTimerPlayerEvent.PausedAtEndOfItem)
        runCurrent()

        assertFalse(player.pauseAtEnd)
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `switching from end of track to countdown clears pause at end`() = runTest {
        val (store, _) = startExecutor()
        store.startEndOfTrack()
        runCurrent()

        store.startCountdown(60_000)
        runCurrent()

        assertFalse(player.pauseAtEnd)
    }

    @Test
    fun `cleared queue cancels timer`() = runTest {
        val (store, _) = startExecutor()
        store.startEndOfTrack()
        runCurrent()

        player.events.emit(SleepTimerPlayerEvent.QueueCleared)
        runCurrent()

        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
        assertFalse(player.pauseAtEnd)
    }

    @Test
    fun `playback end cancels countdown`() = runTest {
        val (store, _) = startExecutor()
        store.startCountdown(60_000)
        runCurrent()

        player.events.emit(SleepTimerPlayerEvent.PlaybackEnded)
        runCurrent()

        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `release restores volume and pause at end`() = runTest {
        val (store, executor) = startExecutor()
        store.startCountdown(15_000)
        advanceTimeBy(10_000)
        runCurrent()

        executor.release()

        assertEquals(0.8f, player.volume)
        assertFalse(player.pauseAtEnd)
    }

    @Test
    fun `deadline passed during device sleep completes silently on resume`() = runTest {
        val (store, _) = startExecutor()
        player.isPlaying = false
        store.startCountdown(30 * 60_000L)
        advanceTimeBy(5 * 60_000L)
        runCurrent()

        sleptMs = 2 * 60 * 60_000L
        player.isPlaying = true
        player.events.emit(SleepTimerPlayerEvent.PlayingChanged(true))
        runCurrent()

        assertEquals(0, player.pauseCalls)
        assertTrue(player.volumeHistory.isEmpty())
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }

    @Test
    fun `restart during fade restores volume before new countdown`() = runTest {
        val (store, _) = startExecutor()
        store.startCountdown(20_000)
        advanceTimeBy(15_000)
        runCurrent()

        store.startCountdown(60_000)
        runCurrent()

        assertEquals(0.8f, player.volume)
        assertEquals(0, player.pauseCalls)
        assertTrue(store.schedule.value is SleepTimerSchedule.Countdown)
    }

    @Test
    fun `playback end completes end of track timer`() = runTest {
        val (store, _) = startExecutor()
        store.startEndOfTrack()
        runCurrent()

        player.events.emit(SleepTimerPlayerEvent.PlaybackEnded)
        runCurrent()

        assertFalse(player.pauseAtEnd)
        assertEquals(SleepTimerSchedule.Off, store.schedule.value)
    }
}
