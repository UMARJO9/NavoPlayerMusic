package tj.umar.navoplayer.core.player.controller

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import tj.umar.navoplayer.core.domain.model.PlaybackProgress
import tj.umar.navoplayer.core.domain.model.PlaybackSource
import tj.umar.navoplayer.core.domain.model.PlaybackState
import tj.umar.navoplayer.core.player.mapper.toDomainRepeatMode
import tj.umar.navoplayer.core.player.mapper.toTrack

@OptIn(UnstableApi::class)
internal fun Player.toPlaybackState(source: PlaybackSource?): PlaybackState {
    val current = currentMediaItem?.toTrack()
    val nextIndex = nextMediaItemIndex
    val next = if (nextIndex != C.INDEX_UNSET) getMediaItemAt(nextIndex).toTrack() else null
    val previousIndex = previousMediaItemIndex
    val previous = if (previousIndex != C.INDEX_UNSET) getMediaItemAt(previousIndex).toTrack() else null
    return PlaybackState(
        currentTrack = current,
        nextTrack = next,
        previousTrack = previous,
        isPlaying = current != null && !Util.shouldShowPlayButton(this),
        shuffleEnabled = shuffleModeEnabled,
        repeatMode = repeatMode.toDomainRepeatMode(),
        source = if (current != null) source else null,
    )
}

internal fun Player.toProgress(): PlaybackProgress {
    val playerDuration = duration.takeIf { it != C.TIME_UNSET && it > 0 }
    val knownDuration = playerDuration ?: currentMediaItem?.mediaMetadata?.durationMs ?: 0L
    return PlaybackProgress(positionMs = currentPosition.coerceAtLeast(0), durationMs = knownDuration)
}
