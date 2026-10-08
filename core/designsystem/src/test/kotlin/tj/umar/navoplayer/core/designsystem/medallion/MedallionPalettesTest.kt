package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class MedallionPalettesTest {

    @Test
    fun `has seven palettes in spec order`() {
        val all = MedallionPalettes.all

        assertEquals(7, all.size)
        assertEquals(MedallionPalette(Color(0xFFC8414B), Color(0xFFF2C14E), Color(0xFF13204A)), all.first())
        assertEquals(MedallionPalette(Color(0xFF8A3B2E), Color(0xFFE3B04B), Color(0xFFF3EEDF)), all.last())
    }

    @Test
    fun `same key always gives same palette`() {
        assertSame(MedallionPalettes.forKey(42), MedallionPalettes.forKey(42))
    }

    @Test
    fun `extreme keys stay in range`() {
        val keys = listOf(Long.MIN_VALUE, Long.MAX_VALUE, -1L, 0L, 0x80000000L, Int.MIN_VALUE.toLong())

        keys.forEach { key -> assertTrue(MedallionPalettes.forKey(key) in MedallionPalettes.all) }
    }

    @Test
    fun `sequential ids use every palette`() {
        val used = (0L until 1000L).map { MedallionPalettes.forKey(it) }.toSet()

        assertEquals(MedallionPalettes.all.toSet(), used)
    }
}
