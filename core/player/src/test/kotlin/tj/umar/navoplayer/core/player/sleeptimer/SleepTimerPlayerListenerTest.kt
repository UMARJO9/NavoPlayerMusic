package tj.umar.navoplayer.core.player.sleeptimer

import androidx.media3.common.Player
import androidx.media3.common.Timeline
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepTimerPlayerListenerTest {

    private val events = mutableListOf<SleepTimerPlayerEvent>()
    private val listener = SleepTimerPlayerListener { events += it }

    @Test
    fun `pause at end of item is reported`() {
        listener.onPlayWhenReadyChanged(false, Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM)
        listener.onPlayWhenReadyChanged(false, Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST)

        assertEquals(listOf(SleepTimerPlayerEvent.PausedAtEndOfItem), events)
    }

    @Test
    fun `ended and playing changes are reported`() {
        listener.onIsPlayingChanged(true)
        listener.onPlaybackStateChanged(Player.STATE_READY)
        listener.onPlaybackStateChanged(Player.STATE_ENDED)

        assertEquals(
            listOf(SleepTimerPlayerEvent.PlayingChanged(true), SleepTimerPlayerEvent.PlaybackEnded),
            events,
        )
    }

    @Test
    fun `empty timeline means queue cleared`() {
        listener.onTimelineChanged(Timeline.EMPTY, Player.TIMELINE_CHANGE_REASON_PLAYLIST_CHANGED)

        assertEquals(listOf(SleepTimerPlayerEvent.QueueCleared), events)
    }
}
