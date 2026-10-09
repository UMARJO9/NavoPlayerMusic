package tj.umar.navoplayer.core.domain.model

sealed interface SleepTimer {
    data object Off : SleepTimer
    data class Countdown(val remainingMs: Long, val durationMs: Long) : SleepTimer
    data object EndOfTrack : SleepTimer
}
