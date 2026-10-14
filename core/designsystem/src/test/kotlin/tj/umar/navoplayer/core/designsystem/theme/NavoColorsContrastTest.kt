package tj.umar.navoplayer.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class NavoColorsContrastTest {

    private fun contrast(foreground: Color, background: Color): Float {
        val first = foreground.luminance()
        val second = background.luminance()
        return (max(first, second) + 0.05f) / (min(first, second) + 0.05f)
    }

    private fun assertReadable(name: String, foreground: Color, background: Color) {
        val ratio = contrast(foreground, background)
        assertTrue("$name contrast $ratio is below 4.5", ratio >= 4.5f)
    }

    @Test
    fun `light text tokens meet AA on every light surface`() {
        val colors = ParchmentNavoColors
        val surfaces = mapOf(
            "background" to colors.background,
            "raised" to colors.raised,
            "card" to colors.card,
        )
        val texts = mapOf(
            "content" to colors.content,
            "contentSecondary" to colors.contentSecondary,
            "contentMuted" to colors.contentMuted,
            "accent" to colors.accent,
        )
        surfaces.forEach { (surfaceName, surface) ->
            texts.forEach { (textName, text) -> assertReadable("$textName on $surfaceName", text, surface) }
        }
    }

    @Test
    fun `light paired tokens meet AA`() {
        val colors = ParchmentNavoColors
        assertReadable("content on high", colors.content, colors.high)
        assertReadable("contentOnHigh on high", colors.contentOnHigh, colors.high)
        assertReadable("onAccent on accent", colors.onAccent, colors.accent)
        assertReadable("onTile on crimson", colors.onTile, colors.crimson)
        assertReadable("onTile on turquoise", colors.onTile, colors.turquoise)
    }

    @Test
    fun `light palette is lighter than dark palette`() {
        assertFalse(ParchmentNavoColors.isDark)
        assertTrue(LapisNavoColors.isDark)
        assertTrue(ParchmentNavoColors.background.luminance() > 0.5f)
        assertTrue(LapisNavoColors.background.luminance() < 0.1f)
    }

    @Test
    fun `dark palette keeps legacy values`() {
        val colors = LapisNavoColors
        assertEquals(Color(0xFF13204A), colors.background)
        assertEquals(Color(0xFF1B2B5E), colors.raised)
        assertEquals(Color(0xFF24387A), colors.high)
        assertEquals(Color(0xFF2A3C78), colors.line)
        assertEquals(Color(0xFFF3EEDF), colors.content)
        assertEquals(Color(0xFFA9B4D6), colors.contentSecondary)
        assertEquals(Color(0xFFC9D1EA), colors.contentMuted)
        assertEquals(Color(0xFFBAC3E0), colors.contentOnHigh)
        assertEquals(Color(0xFFE3B04B), colors.accent)
        assertEquals(Color(0xFF13204A), colors.onAccent)
        assertEquals(Color(0xFF060C24), colors.shadow)
        assertEquals(Color.Transparent, colors.medallionRim)
    }
}
