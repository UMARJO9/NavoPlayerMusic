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
}
