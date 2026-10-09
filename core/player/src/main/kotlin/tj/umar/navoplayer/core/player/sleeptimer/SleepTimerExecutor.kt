package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import tj.umar.navoplayer.core.common.time.NavoClock

internal const val SLEEP_TIMER_FADE_MILLIS = 10_000L

internal class SleepTimerExecutor(
    private val store: SleepTimerStore,
    private val player: SleepTimerPlayer,
    private val clock: NavoClock,
    private val fadeMs: Long = SLEEP_TIMER_FADE_MILLIS,
) {
    private var volumeToRestore: Float? = null

    fun start(scope: CoroutineScope) {
        scope.launch { store.schedule.collectLatest(::run) }
        scope.launch {
            player.events.collect { event ->
                when (event) {
                    SleepTimerPlayerEvent.QueueCleared -> store.cancel()
                    SleepTimerPlayerEvent.PlaybackEnded ->
                        if (store.schedule.value is SleepTimerSchedule.Countdown) store.cancel()
                    else -> Unit
                }
            }
        }
    }

    fun release() {
        volumeToRestore?.let { player.volume = it }
        volumeToRestore = null
        player.setPauseAtEndOfMediaItems(false)
    }

    private suspend fun run(schedule: SleepTimerSchedule) {
        when (schedule) {
            SleepTimerSchedule.Off -> player.setPauseAtEndOfMediaItems(false)
            is SleepTimerSchedule.Countdown -> {
                player.setPauseAtEndOfMediaItems(false)
                runCountdown(schedule)
            }
            is SleepTimerSchedule.EndOfTrack -> runEndOfTrack(schedule)
        }
    }

    private suspend fun runCountdown(schedule: SleepTimerSchedule.Countdown) {
        delayUntil(schedule.endsAtMs - fadeMs)
        if (!player.isPlaying && !awaitResumeBefore(schedule.endsAtMs)) {
            store.complete(schedule)
            return
        }
        val original = player.volume
        volumeToRestore = original
        try {
            player.fadeOut(clock, (schedule.endsAtMs - clock.nowMillis()).coerceIn(0, fadeMs))
            player.pause()
        } finally {
            player.volume = original
            volumeToRestore = null
        }
        store.complete(schedule)
    }

    private suspend fun awaitResumeBefore(deadlineMs: Long): Boolean {
        val left = deadlineMs - clock.nowMillis()
        if (left <= 0) return false
        val resumed = withTimeoutOrNull(left) {
            player.events.first { it == SleepTimerPlayerEvent.PlayingChanged(true) }
        }
        return resumed != null && clock.nowMillis() < deadlineMs
    }

    private suspend fun runEndOfTrack(schedule: SleepTimerSchedule.EndOfTrack) {
        player.setPauseAtEndOfMediaItems(true)
        try {
            player.events.first {
                it == SleepTimerPlayerEvent.PausedAtEndOfItem || it == SleepTimerPlayerEvent.PlaybackEnded
            }
            store.complete(schedule)
        } finally {
            player.setPauseAtEndOfMediaItems(false)
        }
    }

    private suspend fun delayUntil(targetMs: Long) {
        while (true) {
            val wait = targetMs - clock.nowMillis()
            if (wait <= 0) return
            delay(wait)
        }
    }
}
