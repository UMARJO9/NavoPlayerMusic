package tj.umar.navoplayer.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.playback.SleepTimerController
import javax.inject.Inject
import kotlin.time.Duration

class ObserveSleepTimerUseCase @Inject constructor(
    private val controller: SleepTimerController,
) {
    operator fun invoke(): Flow<SleepTimer> = controller.observeSleepTimer()
}

class StartSleepTimerUseCase @Inject constructor(
    private val controller: SleepTimerController,
) {
    operator fun invoke(duration: Duration): Boolean {
        if (duration <= Duration.ZERO) return false
        return controller.startCountdown(duration)
    }
}

class StartEndOfTrackSleepTimerUseCase @Inject constructor(
    private val controller: SleepTimerController,
) {
    operator fun invoke(): Boolean = controller.startEndOfTrack()
}

class CancelSleepTimerUseCase @Inject constructor(
    private val controller: SleepTimerController,
) {
    operator fun invoke() = controller.cancel()
}
