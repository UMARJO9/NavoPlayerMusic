package tj.umar.navoplayer.core.player.nowplaying

import android.os.Looper
import androidx.media3.exoplayer.ExoPlayer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.player.mapper.toMediaItem
import tj.umar.navoplayer.core.testing.data.TestTracks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlayerNowPlayingTest {

    private val player = ExoPlayer.Builder(RuntimeEnvironment.getApplication())
        .setLooper(Looper.getMainLooper())
        .build()

    @After
    fun tearDown() {
        player.release()
    }

    @Test
    fun `empty player is idle`() {
        assertEquals(NowPlaying.Idle, player.toNowPlaying())
    }

    @Test
    fun `loaded paused player exposes track`() {
        player.setMediaItems(listOf(TestTracks.alpha.toMediaItem("q1")))
        shadowOf(Looper.getMainLooper()).idle()

        val nowPlaying = player.toNowPlaying()

        assertEquals(TestTracks.alpha, nowPlaying.track)
        assertFalse(nowPlaying.isPlaying)
    }
}
