package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.database.entity.PlaylistEntity
import tj.umar.navoplayer.core.database.entity.PlaylistTrackEntity
import tj.umar.navoplayer.core.database.model.PlaylistWithTrackRows
import tj.umar.navoplayer.core.domain.model.Playlist

class PlaylistMapperTest {

    @Test
    fun `rows map to playlist ordered by position`() {
        val rows = PlaylistWithTrackRows(
            playlist = PlaylistEntity(id = 3, name = "Mix", createdAt = 10, updatedAt = 20),
            tracks = listOf(
                PlaylistTrackEntity(playlistId = 3, trackId = 30, position = 2),
                PlaylistTrackEntity(playlistId = 3, trackId = 10, position = 0),
                PlaylistTrackEntity(playlistId = 3, trackId = 20, position = 1),
            ),
        )

        assertEquals(Playlist(3, "Mix", 10, 20, listOf(10, 20, 30)), rows.toPlaylist())
    }
}
