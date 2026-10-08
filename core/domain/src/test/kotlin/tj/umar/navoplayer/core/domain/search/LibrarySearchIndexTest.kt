package tj.umar.navoplayer.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.domain.grouping.toAlbums
import tj.umar.navoplayer.core.domain.grouping.toArtists
import tj.umar.navoplayer.core.domain.grouping.toFolders
import tj.umar.navoplayer.core.domain.model.LibraryContent
import tj.umar.navoplayer.core.testing.data.TestSearchTracks

class LibrarySearchIndexTest {

    private val library = TestSearchTracks.library
    private val index = LibrarySearchIndex(
        LibraryContent(library, library.toAlbums(), library.toArtists(), library.toFolders()),
    )

    private fun titles(query: String) = index.search(SearchQuery.parse(query)).tracks.map { it.title }

    @Test
    fun `blank query gives empty results`() {
        assertTrue(index.search(SearchQuery.parse("  ")).isEmpty)
    }

    @Test
    fun `matches title artist and album`() {
        assertEquals(listOf("Blackbird"), titles("beatles"))
        assertEquals(listOf("Ватан", "Ҷавонӣ"), titles("ватан"))
    }

    @Test
    fun `tajik and russian letters match both ways`() {
        assertEquals(listOf("Ҷавонӣ"), titles("чавони"))
        assertEquals(listOf("Ҷавонӣ"), titles("ҷавонӣ"))
    }

    @Test
    fun `yo and accents are ignored`() {
        assertEquals(listOf("Ёлка"), titles("елка"))
        assertEquals(listOf("Déjà Vu"), titles("deja"))
        assertEquals(listOf("Déjà Vu"), titles("beyonce"))
    }

    @Test
    fun `short i is not folded`() {
        assertEquals(listOf("Мой край"), titles("мой"))
    }

    @Test
    fun `words match across fields`() {
        assertEquals(listOf("Ҷавонӣ"), titles("nazarov чавони"))
    }

    @Test
    fun `unmatched word excludes entry`() {
        assertTrue(titles("blackbird zzz").isEmpty())
    }

    @Test
    fun `prefix ranks before word prefix`() {
        assertEquals(listOf("Blackbird", "Back in Black"), titles("black"))
    }

    @Test
    fun `title ranks before artist at same tier`() {
        assertEquals(listOf("Back in Black", "Blackbird", "Déjà Vu"), titles("b"))
    }

    @Test
    fun `single letter matches only at word start`() {
        assertTrue(titles("l").isEmpty())
    }

    @Test
    fun `albums and artists are searched and unknown groups excluded`() {
        val results = index.search(SearchQuery.parse("ватан"))

        assertEquals(listOf("Ватан"), results.albums.map { it.title })
        assertEquals("Daler Nazarov", index.search(SearchQuery.parse("daler")).artists.single().name)
        assertTrue(index.search(SearchQuery.parse("untitled")).artists.isEmpty())
    }

    @Test
    fun `punctuated names match joined and split queries`() {
        assertEquals(listOf("Back in Black"), titles("acdc"))
        assertEquals(listOf("Back in Black"), titles("ac dc"))
    }

    @Test
    fun `exact title ranks before prefix`() {
        val exact = TestSearchTracks.blackbird.copy(id = 50, title = "Black")
        val content = (library + exact).let { LibraryContent(it, it.toAlbums(), it.toArtists(), it.toFolders()) }

        val result = LibrarySearchIndex(content).search(SearchQuery.parse("black")).tracks.map { it.title }

        assertEquals("Black", result.first())
    }

    @Test
    fun `very long query does not crash`() {
        assertTrue(titles("x".repeat(500)).isEmpty())
    }
}
