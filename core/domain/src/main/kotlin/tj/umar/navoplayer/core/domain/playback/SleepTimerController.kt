package tj.umar.navoplayer.core.domain.playback

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.SleepTimer
import kotlin.time.Duration

interface SleepTimerController {
    fun observeSleepTimer(): Flow<SleepTimer>
    fun startCountdown(duration: Duration): Boolean
    fun startEndOfTrack(): Boolean
    fun cancel()
}
