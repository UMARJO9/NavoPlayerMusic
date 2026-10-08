package tj.umar.navoplayer.feature.player.nowplaying

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.RepeatMode
import tj.umar.navoplayer.core.domain.model.Track

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
) {
    val displayedPositionMs: Long
        get() = seekPreviewMs ?: positionMs
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
}

internal sealed interface NowPlayingEffect {
    data object Collapse : NowPlayingEffect
}
