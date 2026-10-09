package tj.umar.navoplayer.core.mediastore.audio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IdSelectionTest {

    @Test
    fun `no ids give no queries`() {
        assertTrue(idSelectionChunks(emptyList()).isEmpty())
    }

    @Test
    fun `single id selects music by id`() {
        val selection = idSelectionChunks(listOf(42L)).single()

        assertEquals("is_music != 0 AND _id IN (?)", selection.selection)
        assertArrayEquals(arrayOf("42"), selection.args)
    }

    @Test
    fun `ids are split into chunks with matching placeholders`() {
        val chunks = idSelectionChunks((1L..501L).toList())

        assertEquals(listOf(500, 1), chunks.map { it.args.size })
        chunks.forEach { chunk ->
            assertEquals(chunk.args.size, chunk.selection.count { it == '?' })
        }
    }

    @Test
    fun `duplicate ids are queried once`() {
        assertArrayEquals(arrayOf("1", "2"), idSelectionChunks(listOf(1L, 2L, 1L)).single().args)
    }
}
