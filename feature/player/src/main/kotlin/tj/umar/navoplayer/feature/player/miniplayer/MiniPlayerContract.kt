package tj.umar.navoplayer.feature.player.miniplayer

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.Track

@Immutable
internal data class MiniPlayerState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
)

internal sealed interface MiniPlayerIntent {
    data object ScreenStarted : MiniPlayerIntent
    data object ScreenStopped : MiniPlayerIntent
    data object PlayPauseClicked : MiniPlayerIntent
    data object OpenClicked : MiniPlayerIntent
}

internal sealed interface MiniPlayerEffect {
    data object OpenNowPlaying : MiniPlayerEffect
}
