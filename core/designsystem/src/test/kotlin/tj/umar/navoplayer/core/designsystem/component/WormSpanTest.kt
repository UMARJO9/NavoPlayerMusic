package tj.umar.navoplayer.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WormSpanTest {

    private val spans = listOf(TabSpan(0f, 100f), TabSpan(110f, 250f), TabSpan(260f, 330f))

    @Test
    fun `empty spans give no indicator`() {
        assertNull(wormSpan(emptyList(), 0f))
    }

    @Test
    fun `whole position matches tab exactly`() {
        assertEquals(spans[1], wormSpan(spans, 1f))
        assertEquals(spans[2], wormSpan(spans, 2f))
    }

    @Test
    fun `position is clamped to tab range`() {
        assertEquals(spans[0], wormSpan(spans, -0.5f))
        assertEquals(spans[2], wormSpan(spans, 7f))
    }

    @Test
    fun `leading edge runs ahead while moving forward`() {
        val span = wormSpan(spans, 0.25f)!!
        val endProgress = (span.end - spans[0].end) / (spans[1].end - spans[0].end)
        val startProgress = (span.start - spans[0].start) / (spans[1].start - spans[0].start)
        assertTrue(endProgress > 0.25f)
        assertTrue(startProgress < 0.25f)
        assertTrue(span.width > spans[0].width)
    }

    @Test
    fun `halfway span stretches across both tabs`() {
        val span = wormSpan(spans, 0.5f)!!
        assertEquals(27.5f, span.start, 0.001f)
        assertEquals(212.5f, span.end, 0.001f)
    }

    @Test
    fun `leading edge runs ahead when tabs go right to left`() {
        val rtl = listOf(TabSpan(200f, 300f), TabSpan(50f, 180f))
        val span = wormSpan(rtl, 0.5f)!!
        assertTrue(span.width > rtl[0].width)
        assertTrue(span.width > rtl[1].width)
        assertEquals(87.5f, span.start, 0.001f)
        assertEquals(270f, span.end, 0.001f)
    }

    @Test
    fun `tabs that fit keep full text size`() {
        val fit = fitTabs(listOf(40f, 60f), available = 300f, minPadding = 10f, minScale = 0.78f)

        assertEquals(1f, fit.textScale, 0.0001f)
        assertEquals(listOf(60f, 80f), fit.weights)
    }

    @Test
    fun `tabs that overflow shrink text to fit`() {
        val fit = fitTabs(listOf(100f, 100f), available = 220f, minPadding = 10f, minScale = 0.5f)

        assertEquals(0.9f, fit.textScale, 0.0001f)
        assertEquals(220f, fit.weights.sum(), 0.001f)
    }

    @Test
    fun `text never shrinks below minimum scale`() {
        val fit = fitTabs(listOf(200f, 200f), available = 100f, minPadding = 10f, minScale = 0.78f)

        assertEquals(0.78f, fit.textScale, 0.0001f)
    }
}
