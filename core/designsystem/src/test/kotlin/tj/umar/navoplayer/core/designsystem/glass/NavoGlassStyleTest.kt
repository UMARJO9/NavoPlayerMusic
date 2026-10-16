package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import tj.umar.navoplayer.core.designsystem.theme.LapisNavoColors
import tj.umar.navoplayer.core.designsystem.theme.ParchmentNavoColors

class NavoGlassStyleTest {

    private val density = Density(2f)
    private val styles = listOf(
        barGlassStyle(LapisNavoColors),
        barGlassStyle(ParchmentNavoColors),
        pillGlassStyle(LapisNavoColors),
        pillGlassStyle(ParchmentNavoColors),
    )

    @Test
    fun `tint tier draws opaque fallback without effects`() {
        styles.forEach { style ->
            val resolved = style.resolve(GlassTier.Tint, density)
            assertEquals(0f, resolved.blurPx)
            assertNull(resolved.refraction)
            assertNull(resolved.colorControls)
            assertNull(resolved.libraryHighlight)
            assertEquals(style.fallbackTint, resolved.tint)
            assertTrue(resolved.drawRim)
        }
    }

    @Test
    fun `blur tier blurs without refraction and keeps own rim`() {
        styles.forEach { style ->
            val resolved = style.resolve(GlassTier.Blur, density)
            assertEquals(style.blurRadius.value * 2f, resolved.blurPx)
            assertNull(resolved.refraction)
            assertNotNull(resolved.colorControls)
            assertNull(resolved.libraryHighlight)
            assertEquals(style.tint, resolved.tint)
            assertTrue(resolved.drawRim)
        }
    }

    @Test
    fun `refraction tier adds lens and library highlight`() {
        styles.forEach { style ->
            val resolved = style.resolve(GlassTier.Refraction, density)
            val refraction = requireNotNull(resolved.refraction)
            assertEquals(style.refractionHeight.value * 2f, refraction.heightPx)
            assertEquals(style.refractionAmount.value * 2f, refraction.amountPx)
            assertFalse(refraction.chromaticAberration)
            assertEquals(style.highlightAlpha, resolved.libraryHighlight)
            assertFalse(resolved.drawRim)
        }
    }

    @Test
    fun `glass tint is translucent and fallback is denser`() {
        styles.forEach { style ->
            assertTrue(style.tint.alpha < 1f)
            assertTrue(style.fallbackTint.alpha > style.tint.alpha)
            assertTrue(style.rimTop.alpha > style.rimBottom.alpha)
        }
    }

    @Test
    fun `refraction never exceeds half of the smallest dimension`() {
        val refraction = requireNotNull(barGlassStyle(LapisNavoColors).resolve(GlassTier.Refraction, density).refraction)
        listOf(Size(400f, 20f), Size(10f, 10f), Size(1000f, 136f), Size(0f, 0f)).forEach { size ->
            val fitted = refraction.fitTo(size)
            assertTrue(fitted.heightPx <= size.minDimension / 2f)
            assertTrue(fitted.heightPx >= 0f)
            assertEquals(refraction.amountPx, fitted.amountPx)
        }
    }

    @Test
    fun `refraction is unchanged when it fits`() {
        val refraction = requireNotNull(barGlassStyle(LapisNavoColors).resolve(GlassTier.Refraction, density).refraction)
        assertEquals(refraction, refraction.fitTo(Size(1000f, 1000f)))
    }
}
