package tj.umar.navoplayer.core.domain.grouping

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.Album
import tj.umar.navoplayer.core.domain.model.Artist
import tj.umar.navoplayer.core.domain.model.Folder
import tj.umar.navoplayer.core.domain.model.TrackGroupKey
import tj.umar.navoplayer.core.domain.model.TrackGroupType
import tj.umar.navoplayer.core.testing.data.TestTracks
import java.text.Collator
import java.util.Locale

class TrackGroupingTest {

    private val collator = Collator.getInstance(Locale.forLanguageTag("ru")).apply { strength = Collator.PRIMARY }
    private val library = TestTracks.library

    @Test
    fun `albums are grouped by id`() {
        val first = library.toAlbums(collator).single { it.key == TrackGroupKey(TrackGroupType.Album, 10, null) }

        assertEquals("First", first.title)
        assertEquals(listOf(TestTracks.alpha, TestTracks.alphaTwo), first.tracks)
    }

    @Test
    fun `album without id is grouped by name`() {
        val album = library.toAlbums(collator).single { it.title == "Ёлка" }

        assertEquals(TrackGroupKey(TrackGroupType.Album, null, "Ёлка"), album.key)
    }

    @Test
    fun `unknown album bucket is last`() {
        val albums = library.toAlbums(collator)

        assertTrue(albums.last().key.isUnknown)
        assertEquals(listOf(TestTracks.beta), albums.last().tracks)
    }

    @Test
    fun `missing album name with id is unknown`() {
        val track = TestTracks.alpha.copy(album = null)

        assertTrue(track.groupKey(TrackGroupType.Album).isUnknown)
    }

    @Test
    fun `album artist is single or various`() {
        val albums = library.toAlbums(collator)
        val first = albums.single { it.title == "First" }
        val mixes = albums.single { it.title == "Mixes" }

        assertNull(first.artist)
        assertTrue(first.hasVariousArtists)
        assertEquals("DJ Navo", mixes.artist)
        assertFalse(mixes.hasVariousArtists)
    }

    @Test
    fun `album tracks follow track numbers with unnumbered last`() {
        val unnumbered = TestTracks.alpha.copy(id = 9, title = "Aaa", trackNumber = null)
        val album = (library + unnumbered).toAlbums(collator).single { it.title == "First" }

        assertEquals(listOf(1L, 4L, 9L), album.tracks.map { it.id })
    }

    @Test
    fun `cyrillic names sort alphabetically ignoring case`() {
        val tracks = listOf("яблоко", "Арбуз", "банан").mapIndexed { index, name ->
            TestTracks.alpha.copy(id = index.toLong(), album = name, albumId = null)
        }

        val titles = tracks.toAlbums(collator).map { it.title }

        assertEquals(listOf("Арбуз", "банан", "яблоко"), titles)
    }

    @Test
    fun `artist album count ignores unknown album`() {
        val withoutAlbum = TestTracks.longMix.copy(id = 8, album = null, albumId = null)
        val artist = (library + withoutAlbum).toArtists(collator).single { it.name == "DJ Navo" }

        assertEquals(1, artist.albumCount)
        assertEquals(2, artist.tracks.size)
    }

    @Test
    fun `artist without id is grouped by name and unknown is last`() {
        val artists = library.toArtists(collator)

        assertEquals(TrackGroupKey(TrackGroupType.Artist, null, "Ахмад"), artists.single { it.name == "Ахмад" }.key)
        assertTrue(artists.last().key.isUnknown)
    }

    @Test
    fun `folder name is last segment and path is kept`() {
        val folder = library.toFolders(collator).single { it.path == "Music/Navo" }

        assertEquals("Navo", folder.name)
        assertEquals(listOf(TestTracks.alpha, TestTracks.alphaTwo), folder.tracks)
    }

    @Test
    fun `tracks without folder go to unknown folder last`() {
        val folders = library.toFolders(collator)

        assertTrue(folders.last().key.isUnknown)
        assertNull(folders.last().name)
    }

    @Test
    fun `groupFor finds matching group`() {
        val key = TrackGroupKey(TrackGroupType.Folder, null, "Music/Mixes")

        val group = library.groupFor(key, collator)

        assertTrue(group is Folder)
        assertEquals(listOf(TestTracks.longMix), group?.tracks)
    }

    @Test
    fun `groupFor works for unknown bucket and returns null when absent`() {
        val unknownArtist = library.groupFor(TrackGroupKey(TrackGroupType.Artist, null, null), collator)

        assertTrue(unknownArtist is Artist)
        assertEquals(listOf(TestTracks.beta), unknownArtist?.tracks)
        assertNull(library.groupFor(TrackGroupKey(TrackGroupType.Album, 999, null), collator))
        assertTrue(library.groupFor(TrackGroupKey(TrackGroupType.Album, 11, null), collator) is Album)
    }

    @Test
    fun `multi disc album plays disc by disc`() {
        val disc = { id: Long, discNumber: Int, number: Int ->
            TestTracks.alpha.copy(id = id, title = "T$id", discNumber = discNumber, trackNumber = number)
        }
        val tracks = listOf(disc(1, 2, 1), disc(2, 1, 2), disc(3, 2, 2), disc(4, 1, 1))

        val album = tracks.toAlbums(collator).single()

        assertEquals(listOf(4L, 2L, 1L, 3L), album.tracks.map { it.id })
    }

    @Test
    fun `artist keeps same named albums apart`() {
        val first = TestTracks.longMix.copy(id = 21, title = "B", album = "Hits", albumId = 1, trackNumber = 1)
        val second = TestTracks.longMix.copy(id = 22, title = "A", album = "Hits", albumId = 2, trackNumber = 1)
        val firstTwo = TestTracks.longMix.copy(id = 23, title = "C", album = "Hits", albumId = 1, trackNumber = 2)

        val artist = listOf(second, firstTwo, first).toArtists(collator).single()

        assertEquals(listOf(21L, 23L, 22L), artist.tracks.map { it.id })
    }

    @Test
    fun `same named folders in different paths stay separate`() {
        val rock = TestTracks.alpha.copy(id = 31, folderPath = "Music/Rock")
        val downloadsRock = TestTracks.alpha.copy(id = 32, folderPath = "Download/Rock")

        val folders = listOf(rock, downloadsRock).toFolders(collator)

        assertEquals(listOf("Download/Rock", "Music/Rock"), folders.map { it.path })
    }
}
