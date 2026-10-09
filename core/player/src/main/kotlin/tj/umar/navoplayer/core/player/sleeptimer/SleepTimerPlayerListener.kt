package tj.umar.navoplayer.core.player.sleeptimer

import androidx.media3.common.Player
import androidx.media3.common.Timeline

internal class SleepTimerPlayerListener(
    private val emit: (SleepTimerPlayerEvent) -> Unit,
) : Player.Listener {

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        emit(SleepTimerPlayerEvent.PlayingChanged(isPlaying))
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (!playWhenReady && reason == Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
            emit(SleepTimerPlayerEvent.PausedAtEndOfItem)
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) emit(SleepTimerPlayerEvent.PlaybackEnded)
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        if (timeline.isEmpty) emit(SleepTimerPlayerEvent.QueueCleared)
    }
}
