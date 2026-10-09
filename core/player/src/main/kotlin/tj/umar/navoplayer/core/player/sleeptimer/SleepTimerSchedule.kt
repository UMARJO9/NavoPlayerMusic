package tj.umar.navoplayer.core.player.sleeptimer

internal sealed interface SleepTimerSchedule {
    data object Off : SleepTimerSchedule
    data class Countdown(val id: Long, val endsAtMs: Long, val durationMs: Long) : SleepTimerSchedule
    data class EndOfTrack(val id: Long) : SleepTimerSchedule
}
