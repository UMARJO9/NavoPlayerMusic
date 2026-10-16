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
    fun `centered scroll puts tab center in viewport middle`() {
        assertEquals(150, centeredScroll(tabCenter = 350f, viewport = 400, maxScroll = 500, rtl = false))
        assertEquals(350, centeredScroll(tabCenter = 350f, viewport = 400, maxScroll = 500, rtl = true))
    }

    @Test
    fun `centered scroll is clamped`() {
        assertEquals(0, centeredScroll(tabCenter = 50f, viewport = 400, maxScroll = 500, rtl = false))
        assertEquals(500, centeredScroll(tabCenter = 50f, viewport = 400, maxScroll = 500, rtl = true))
        assertEquals(500, centeredScroll(tabCenter = 2000f, viewport = 400, maxScroll = 500, rtl = false))
    }
}
