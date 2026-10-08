package tj.umar.navoplayer.core.designsystem.equalizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerBarsTest {

    private val tolerance = 0.0001f

    @Test
    fun `starts at minimum scale`() {
        assertEquals(MIN_SCALE, barScale(0), tolerance)
    }

    @Test
    fun `reaches full height at half cycle`() {
        assertEquals(1f, barScale(900), tolerance)
    }

    @Test
    fun `returns to minimum after full cycle`() {
        assertEquals(MIN_SCALE, barScale(1_800), tolerance)
    }

    @Test
    fun `is symmetric around the peak`() {
        assertEquals(barScale(600), barScale(1_200), tolerance)
    }

    @Test
    fun `stays within bounds`() {
        (0L..3_600L step 37).forEach { time ->
            val scale = barScale(time)
            assertTrue(scale >= MIN_SCALE - tolerance && scale <= 1f + tolerance)
        }
    }
}
