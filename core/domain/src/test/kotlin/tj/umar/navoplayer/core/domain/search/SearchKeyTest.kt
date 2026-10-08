package tj.umar.navoplayer.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.Normalizer

class SearchKeyTest {

    @Test
    fun `folds case for latin and cyrillic`() {
        assertEquals("hello мир", "HeLLo МИР".toSearchKey())
    }

    @Test
    fun `yo becomes ye`() {
        assertEquals("елка".toSearchKey(), "Ёлка".toSearchKey())
    }

    @Test
    fun `short i stays distinct`() {
        assertNotEquals("Мои".toSearchKey(), "Мой".toSearchKey())
    }

    @Test
    fun `tajik letters fold to russian base`() {
        assertEquals("чавони", "Ҷавонӣ".toSearchKey())
        assertEquals("хаво кух гул у", "Ҳаво Қӯҳ Ғул Ӯ".toSearchKey())
    }

    @Test
    fun `accents are removed`() {
        assertEquals("beyonce", "Beyoncé".toSearchKey())
        assertEquals("motley crue", "Mötley Crüe".toSearchKey())
        assertEquals("strasse", "Straße".toSearchKey())
    }

    @Test
    fun `decomposed input matches composed input`() {
        val decomposed = Normalizer.normalize("Déjà Vu", Normalizer.Form.NFD)

        assertEquals("Déjà Vu".toSearchKey(), decomposed.toSearchKey())
    }

    @Test
    fun `punctuation becomes word breaks`() {
        assertEquals("ac dc", "AC/DC".toSearchKey())
    }

    @Test
    fun `whitespace collapses and blank is empty`() {
        assertEquals("a b", "  a   b  ".toSearchKey())
        assertTrue("  \t ".toSearchKey().isEmpty())
    }
}
