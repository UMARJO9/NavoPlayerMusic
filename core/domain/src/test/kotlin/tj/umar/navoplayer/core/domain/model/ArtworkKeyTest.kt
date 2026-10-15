package tj.umar.navoplayer.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtworkKeyTest {

    private val track = Track(
        id = 7,
        title = "Song",
        artist = "Artist",
        album = "Album",
        albumId = 70,
        artistId = 700,
        durationMs = 1_000,
        trackNumber = 1,
        contentUri = "content://media/external/audio/media/7",
        folderPath = "Music",
        discNumber = null,
        albumArtist = null,
    )

    @Test
    fun `track key keeps album id when album is known`() {
        assertEquals(ArtworkKey(trackId = 7, albumId = 70), track.artworkKey)
    }

    @Test
    fun `track key drops album id when album is unknown`() {
        assertEquals(ArtworkKey(trackId = 7, albumId = null), track.copy(album = null).artworkKey)
    }

    @Test
    fun `album group key uses first track and album id`() {
        val second = track.copy(id = 8)
        val album = Album(
            key = TrackGroupKey(TrackGroupType.Album, id = 70, name = "Album"),
            title = "Album",
            artist = "Artist",
            hasVariousArtists = false,
            tracks = listOf(track, second),
        )

        assertEquals(ArtworkKey(trackId = 7, albumId = 70), album.artworkKey())
    }

    @Test
    fun `unknown album group has no key`() {
        val album = Album(
            key = TrackGroupKey(TrackGroupType.Album, id = null, name = null),
            title = null,
            artist = null,
            hasVariousArtists = false,
            tracks = listOf(track),
        )

        assertNull(album.artworkKey())
    }

    @Test
    fun `empty album group has no key`() {
        val album = Album(
            key = TrackGroupKey(TrackGroupType.Album, id = 70, name = "Album"),
            title = "Album",
            artist = null,
            hasVariousArtists = false,
            tracks = emptyList(),
        )

        assertNull(album.artworkKey())
    }

    @Test
    fun `artist and folder groups have no key`() {
        val artist = Artist(
            key = TrackGroupKey(TrackGroupType.Artist, id = 700, name = "Artist"),
            name = "Artist",
            albumCount = 1,
            tracks = listOf(track),
        )
        val folder = Folder(
            key = TrackGroupKey(TrackGroupType.Folder, id = null, name = "Music"),
            name = "Music",
            path = "Music",
            tracks = listOf(track),
        )

        assertNull(artist.artworkKey())
        assertNull(folder.artworkKey())
    }
}
