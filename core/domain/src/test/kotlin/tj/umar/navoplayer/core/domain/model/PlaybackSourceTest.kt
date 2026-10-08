package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackSourceTest {

    @Test
    fun `playlist source matches by id regardless of name`() {
        val source: PlaybackSource = PlaybackSource.Playlist(id = 4, name = "Old name")

        assertTrue(source.isPlaylist(4))
        assertFalse(source.isPlaylist(5))
    }

    @Test
    fun `other sources are not playlists`() {
        assertFalse(PlaybackSource.AllTracks.isPlaylist(4))
        assertFalse((null as PlaybackSource?).isPlaylist(4))
    }

    @Test
    fun `favorites source is detected`() {
        assertTrue(PlaybackSource.Favorites.isFavorites())
        assertFalse(PlaybackSource.AllTracks.isFavorites())
        assertFalse((null as PlaybackSource?).isFavorites())
    }
}
