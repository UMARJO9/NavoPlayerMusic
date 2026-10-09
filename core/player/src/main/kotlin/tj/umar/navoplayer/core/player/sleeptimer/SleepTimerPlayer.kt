package tj.umar.navoplayer.core.player.sleeptimer

import kotlinx.coroutines.flow.Flow

internal interface SleepTimerPlayer {
    val isPlaying: Boolean
    val hasEnded: Boolean
    var volume: Float
    val events: Flow<SleepTimerPlayerEvent>
    fun pause()
    fun setPauseAtEndOfMediaItems(enabled: Boolean)
}

internal sealed interface SleepTimerPlayerEvent {
    data class PlayingChanged(val isPlaying: Boolean) : SleepTimerPlayerEvent
    data object PausedAtEndOfItem : SleepTimerPlayerEvent
    data object PlaybackEnded : SleepTimerPlayerEvent
    data object QueueCleared : SleepTimerPlayerEvent
}
