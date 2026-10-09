package tj.umar.navoplayer.core.testing.playback

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.playback.SleepTimerController
import kotlin.time.Duration

sealed interface SleepTimerCommand {
    data class Start(val duration: Duration) : SleepTimerCommand
    data object StartEndOfTrack : SleepTimerCommand
    data object Cancel : SleepTimerCommand
}

class FakeSleepTimerController : SleepTimerController {

    val timer = MutableStateFlow<SleepTimer>(SleepTimer.Off)

    var acceptStart: Boolean = true

    private val recorded = mutableListOf<SleepTimerCommand>()
    val commands: List<SleepTimerCommand> get() = recorded.toList()

    var subscribers: Int = 0
        private set

    override fun observeSleepTimer(): Flow<SleepTimer> = timer
        .onStart { subscribers++ }
        .onCompletion { subscribers-- }

    override fun startCountdown(duration: Duration): Boolean {
        recorded += SleepTimerCommand.Start(duration)
        if (acceptStart) timer.value = SleepTimer.Countdown(duration.inWholeMilliseconds, duration.inWholeMilliseconds)
        return acceptStart
    }

    override fun startEndOfTrack(): Boolean {
        recorded += SleepTimerCommand.StartEndOfTrack
        if (acceptStart) timer.value = SleepTimer.EndOfTrack
        return acceptStart
    }

    override fun cancel() {
        recorded += SleepTimerCommand.Cancel
        timer.value = SleepTimer.Off
    }
}
