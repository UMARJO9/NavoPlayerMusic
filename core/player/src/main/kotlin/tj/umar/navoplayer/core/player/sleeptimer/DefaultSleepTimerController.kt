package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import tj.umar.navoplayer.core.common.time.ElapsedRealtimeClock
import tj.umar.navoplayer.core.common.time.NavoClock
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.playback.SleepTimerController
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

private const val SECOND_MILLIS = 1_000L

@Singleton
internal class DefaultSleepTimerController @Inject constructor(
    private val store: SleepTimerStore,
    @param:ElapsedRealtimeClock private val clock: NavoClock,
) : SleepTimerController {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSleepTimer(): Flow<SleepTimer> = store.schedule
        .flatMapLatest { schedule ->
            when (schedule) {
                SleepTimerSchedule.Off -> flowOf(SleepTimer.Off)
                is SleepTimerSchedule.EndOfTrack -> flowOf(SleepTimer.EndOfTrack)
                is SleepTimerSchedule.Countdown -> countdown(schedule)
            }
        }
        .distinctUntilChanged()

    override fun startCountdown(duration: Duration): Boolean = store.startCountdown(duration.inWholeMilliseconds)

    override fun startEndOfTrack(): Boolean = store.startEndOfTrack()

    override fun cancel() = store.cancel()

    private fun countdown(schedule: SleepTimerSchedule.Countdown): Flow<SleepTimer> = flow {
        while (true) {
            val remaining = (schedule.endsAtMs - clock.nowMillis()).coerceAtLeast(0)
            emit(SleepTimer.Countdown(remaining, schedule.durationMs))
            if (remaining == 0L) break
            val toNextSecond = remaining % SECOND_MILLIS
            delay(if (toNextSecond == 0L) SECOND_MILLIS else toNextSecond)
        }
    }
}
