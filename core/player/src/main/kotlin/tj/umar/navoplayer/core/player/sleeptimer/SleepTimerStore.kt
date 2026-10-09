package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tj.umar.navoplayer.core.common.time.ElapsedRealtimeClock
import tj.umar.navoplayer.core.common.time.NavoClock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SleepTimerStore @Inject constructor(
    @param:ElapsedRealtimeClock private val clock: NavoClock,
) {
    private val state = MutableStateFlow<SleepTimerSchedule>(SleepTimerSchedule.Off)
    private val attached = AtomicBoolean(false)
    private val ids = AtomicLong(0)

    val schedule: StateFlow<SleepTimerSchedule> = state.asStateFlow()

    fun attach() {
        attached.set(true)
    }

    fun detach() {
        attached.set(false)
        state.value = SleepTimerSchedule.Off
    }

    fun startCountdown(durationMs: Long): Boolean {
        if (!attached.get() || durationMs <= 0) return false
        state.value = SleepTimerSchedule.Countdown(ids.incrementAndGet(), clock.nowMillis() + durationMs, durationMs)
        return true
    }

    fun startEndOfTrack(): Boolean {
        if (!attached.get()) return false
        state.value = SleepTimerSchedule.EndOfTrack(ids.incrementAndGet())
        return true
    }

    fun cancel() {
        state.value = SleepTimerSchedule.Off
    }

    fun complete(expected: SleepTimerSchedule) {
        state.compareAndSet(expected, SleepTimerSchedule.Off)
    }
}
