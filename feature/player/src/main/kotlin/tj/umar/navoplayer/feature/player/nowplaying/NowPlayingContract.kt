package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.SleepTimer
import tj.umar.navoplayer.core.domain.model.Track

private const val MINUTE_MILLIS = 60_000L

@Immutable
internal data class NowPlayingState(
    val isLoading: Boolean = true,
    val track: Track? = null,
    val nextTrack: Track? = null,
    val source: PlaybackSource? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val seekPreviewMs: Long? = null,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val isFavorite: Boolean = false,
    val sleepTimer: SleepTimer = SleepTimer.Off,
    val isSleepTimerSheetVisible: Boolean = false,
) {
    val displayedPositionMs: Long
        get() = seekPreviewMs ?: positionMs
}

internal sealed interface SleepTimerOption {
    data class Minutes(val minutes: Int) : SleepTimerOption
    data object EndOfTrack : SleepTimerOption

    companion object {
        val presets: List<SleepTimerOption> = listOf(5, 15, 30, 45, 60).map(::Minutes) + EndOfTrack
    }
}

internal fun SleepTimer.selectedOption(): SleepTimerOption? = when (this) {
    SleepTimer.Off -> null
    SleepTimer.EndOfTrack -> SleepTimerOption.EndOfTrack
    is SleepTimer.Countdown -> SleepTimerOption.presets.firstOrNull {
        it is SleepTimerOption.Minutes && it.minutes * MINUTE_MILLIS == durationMs
    }
}

internal sealed interface NowPlayingIntent {
    data object ScreenStarted : NowPlayingIntent
    data object ScreenStopped : NowPlayingIntent
    data object PlayPauseClicked : NowPlayingIntent
    data object NextClicked : NowPlayingIntent
    data object PreviousClicked : NowPlayingIntent
    data object ShuffleClicked : NowPlayingIntent
    data object RepeatClicked : NowPlayingIntent
    data class SeekChanged(val positionMs: Long) : NowPlayingIntent
    data object SeekFinished : NowPlayingIntent
    data object FavoriteClicked : NowPlayingIntent
    data object CollapseClicked : NowPlayingIntent
    data object MoreClicked : NowPlayingIntent
    data object QueueClicked : NowPlayingIntent
    data object SleepTimerClicked : NowPlayingIntent
    data object SleepTimerSheetDismissed : NowPlayingIntent
    data class SleepTimerOptionSelected(val option: SleepTimerOption) : NowPlayingIntent
    data object SleepTimerCancelClicked : NowPlayingIntent
}

internal sealed interface NowPlayingEffect {
    data object Collapse : NowPlayingEffect
    data object OpenQueue : NowPlayingEffect
    data class ShowMessage(val message: NowPlayingMessage) : NowPlayingEffect
}

internal sealed interface NowPlayingMessage {
    data object FavoriteFailed : NowPlayingMessage
    data class SleepTimerSet(val minutes: Int) : NowPlayingMessage
    data object SleepTimerEndOfTrack : NowPlayingMessage
    data object SleepTimerOff : NowPlayingMessage
    data object SleepTimerUnavailable : NowPlayingMessage
}
