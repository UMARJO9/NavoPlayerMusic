package tj.umar.navoplayer.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `text is trimmed`() {
        assertEquals("Ҷавонӣ", SearchQuery.parse("  Ҷавонӣ ").text)
    }

    @Test
    fun `tokens are distinct`() {
        assertEquals(listOf("la"), SearchQuery.parse("La la LA").tokens)
    }

    @Test
    fun `whitespace query is blank`() {
        assertTrue(SearchQuery.parse("   ").isBlank)
    }
}
