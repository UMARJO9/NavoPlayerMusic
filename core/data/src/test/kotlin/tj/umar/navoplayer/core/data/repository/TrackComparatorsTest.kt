package tj.umar.navoplayer.core.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import tj.umar.navoplayer.core.testing.data.TestTracks
import java.text.Collator
import java.util.Locale

class TrackComparatorsTest {

    private val comparator = trackTitleComparator(Collator.getInstance(Locale.ENGLISH))

    private fun track(id: Long, title: String) = TestTracks.alpha.copy(id = id, title = title)

    @Test
    fun `ignores case`() {
        val sorted = listOf(track(1, "beta"), track(2, "Alpha")).sortedWith(comparator)
        assertEquals(listOf("Alpha", "beta"), sorted.map { it.title })
    }

    @Test
    fun `ignores accents`() {
        val sorted = listOf(track(1, "Ébène"), track(2, "Echo")).sortedWith(comparator)
        assertEquals(listOf("Ébène", "Echo"), sorted.map { it.title })
    }

    @Test
    fun `sorts cyrillic titles alphabetically`() {
        val sorted = listOf(track(1, "яблоко"), track(2, "Арбуз"), track(3, "банан"))
            .sortedWith(trackTitleComparator(Collator.getInstance(Locale.forLanguageTag("ru"))))
        assertEquals(listOf("Арбуз", "банан", "яблоко"), sorted.map { it.title })
    }

    @Test
    fun `equal titles are ordered by id`() {
        val sorted = listOf(track(3, "Song"), track(1, "song")).sortedWith(comparator)
        assertEquals(listOf(1L, 3L), sorted.map { it.id })
    }
}
