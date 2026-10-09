package tj.umar.navoplayer.feature.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.NowPlaying
import tj.umar.navoplayer.core.testing.data.TestTracks

class NavoWidgetUiStateTest {

    @Test
    fun `idle maps to empty`() {
        assertEquals(NavoWidgetUiState.Empty, NowPlaying.Idle.toWidgetUiState())
    }

    @Test
    fun `track maps to playing state`() {
        val state = NowPlaying(TestTracks.alpha, isPlaying = true).toWidgetUiState()

        assertEquals(
            NavoWidgetUiState.Playing(TestTracks.alpha.id, TestTracks.alpha.title, TestTracks.alpha.artist, isPlaying = true),
            state,
        )
    }

    @Test
    fun `blank title and artist become null`() {
        val track = TestTracks.alpha.copy(title = " ", artist = "")

        val state = NowPlaying(track, isPlaying = false).toWidgetUiState() as NavoWidgetUiState.Playing

        assertEquals(null, state.title)
        assertEquals(null, state.artist)
    }

    @Test
    fun `same input gives equal state`() {
        val nowPlaying = NowPlaying(TestTracks.beta, isPlaying = false)

        assertEquals(nowPlaying.toWidgetUiState(), nowPlaying.copy().toWidgetUiState())
    }
}
