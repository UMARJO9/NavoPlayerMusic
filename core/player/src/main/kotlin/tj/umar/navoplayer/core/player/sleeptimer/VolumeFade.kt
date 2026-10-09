package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.delay
import tj.umar.navoplayer.core.common.time.NavoClock

private const val FADE_STEP_MILLIS = 50L

internal suspend fun SleepTimerPlayer.fadeOut(clock: NavoClock, durationMs: Long) {
    val startVolume = volume
    if (durationMs <= 0) {
        volume = 0f
        return
    }
    val startedAt = clock.nowMillis()
    while (true) {
        val progress = ((clock.nowMillis() - startedAt).toFloat() / durationMs).coerceIn(0f, 1f)
        val left = 1f - progress
        volume = startVolume * left * left
        if (progress >= 1f) return
        delay(FADE_STEP_MILLIS)
    }
}
