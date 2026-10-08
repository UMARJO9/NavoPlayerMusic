package tj.umar.navoplayer.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.Track
import tj.umar.navoplayer.core.mediastore.audio.MediaStoreAudioRow

class TrackMapperTest {

    private val row = MediaStoreAudioRow(
        id = 7,
        title = "Song",
        displayName = "song.mp3",
        artist = "Artist",
        artistId = 70,
        album = "Album",
        albumId = 700,
        durationMs = 200_000,
        track = 3,
        contentUri = "content://media/external/audio/media/7",
        relativePath = "Music/Navo/",
    )

    @Test
    fun `maps all fields`() {
        val expected = Track(
            id = 7,
            title = "Song",
            artist = "Artist",
            album = "Album",
            albumId = 700,
            artistId = 70,
            durationMs = 200_000,
            trackNumber = 3,
            contentUri = "content://media/external/audio/media/7",
            folderPath = "Music/Navo",
            discNumber = null,
        )
        assertEquals(expected, row.toTrack())
    }

    @Test
    fun `null blank and unknown tags become null`() {
        listOf(null, "  ", "<unknown>", "<UNKNOWN>").forEach { value ->
            val track = row.copy(artist = value, album = value).toTrack()
            assertNull(track.artist)
            assertNull(track.album)
        }
    }

    @Test
    fun `missing title falls back to file name without extension`() {
        assertEquals("my.song", row.copy(title = null, displayName = "my.song.flac").toTrack().title)
        assertEquals("song", row.copy(title = "<unknown>").toTrack().title)
    }

    @Test
    fun `missing title and file name give empty title`() {
        assertEquals("", row.copy(title = null, displayName = null).toTrack().title)
    }

    @Test
    fun `null or negative duration becomes zero`() {
        assertEquals(0L, row.copy(durationMs = null).toTrack().durationMs)
        assertEquals(0L, row.copy(durationMs = -5).toTrack().durationMs)
    }

    @Test
    fun `non positive ids become null`() {
        val track = row.copy(albumId = 0, artistId = null).toTrack()
        assertNull(track.albumId)
        assertNull(track.artistId)
    }

    @Test
    fun `track number drops disc prefix`() {
        assertEquals(5, row.copy(track = 1005).toTrack().trackNumber)
        assertEquals(1, row.copy(track = 1005).toTrack().discNumber)
        assertEquals(2, row.copy(track = 2003).toTrack().discNumber)
        assertNull(row.copy(track = 7).toTrack().discNumber)
        assertNull(row.copy(track = 0).toTrack().trackNumber)
        assertNull(row.copy(track = 2000).toTrack().trackNumber)
        assertNull(row.copy(track = null).toTrack().trackNumber)
    }

    @Test
    fun `relative path loses trailing slash and blank becomes null`() {
        assertEquals("Music/Rock", folderPathOf("Music/Rock/", null))
        assertNull(folderPathOf("  ", null))
    }

    @Test
    fun `legacy data path strips storage root and file name`() {
        assertEquals("Music", folderPathOf(null, "/storage/emulated/0/Music/a.mp3"))
        assertEquals("Songs/x", folderPathOf(null, "/storage/1234-ABCD/Songs/x/a.mp3"))
        assertEquals("Music", folderPathOf(null, "/sdcard/Music/a.mp3"))
    }

    @Test
    fun `file at storage root has no folder`() {
        assertNull(folderPathOf(null, "/storage/emulated/0/a.mp3"))
        assertNull(folderPathOf(null, null))
    }

    @Test
    fun `relative path wins over data path`() {
        assertEquals("Music/New", folderPathOf("Music/New/", "/storage/emulated/0/Old/a.mp3"))
    }
}
