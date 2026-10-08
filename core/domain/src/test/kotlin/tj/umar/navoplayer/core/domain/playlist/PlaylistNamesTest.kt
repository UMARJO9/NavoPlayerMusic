package tj.umar.navoplayer.core.domain.playlist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistNamesTest {

    @Test
    fun `name is trimmed and inner whitespace collapsed`() {
        assertEquals("Утро в горах", normalizePlaylistName("  Утро \t в\n горах  "))
    }

    @Test
    fun `blank name is invalid`() {
        assertNull(normalizePlaylistName("   "))
        assertNull(normalizePlaylistName(""))
    }

    @Test
    fun `name at max length is valid and longer is invalid`() {
        val max = "a".repeat(PLAYLIST_NAME_MAX_LENGTH)

        assertEquals(max, normalizePlaylistName(max))
        assertNull(normalizePlaylistName(max + "a"))
    }
}
