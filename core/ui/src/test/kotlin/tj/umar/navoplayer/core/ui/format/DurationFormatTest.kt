package tj.umar.navoplayer.core.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatTest {

    @Test
    fun `formats minutes and seconds`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:59", formatDuration(59_999))
        assertEquals("1:01", formatDuration(61_000))
        assertEquals("59:59", formatDuration(3_599_000))
    }

    @Test
    fun `formats hours`() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("1:02:05", formatDuration(3_725_000))
    }

    @Test
    fun `negative duration becomes zero`() {
        assertEquals("0:00", formatDuration(-1_000))
    }
}
