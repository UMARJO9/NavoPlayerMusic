package tj.umar.navoplayer.core.player.controller

import android.os.Looper
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerStateMapperTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()
    private val tracks = (1..3).map { TestTracks.alpha.copy(id = it.toLong()) }

    @After
    fun tearDown() {
        player.release()
    }

    private fun load(startIndex: Int) {
        player.setMediaItems(tracks.mapIndexed { index, track -> track.toMediaItem("q$index") }, startIndex, 0)
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `neighbours of middle item are mapped`() {
        load(startIndex = 1)

        val state = player.toPlaybackState(null)

        assertEquals(tracks[0], state.previousTrack)
        assertEquals(tracks[2], state.nextTrack)
    }

    @Test
    fun `first item has no previous track until repeat all`() {
        load(startIndex = 0)
        assertNull(player.toPlaybackState(null).previousTrack)

        player.repeatMode = Player.REPEAT_MODE_ALL
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(tracks[2], player.toPlaybackState(null).previousTrack)
    }
}
