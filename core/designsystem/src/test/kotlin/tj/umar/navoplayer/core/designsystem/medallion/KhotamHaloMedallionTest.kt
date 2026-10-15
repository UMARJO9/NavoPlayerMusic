package tj.umar.navoplayer.core.designsystem.medallion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KhotamHaloMedallionTest {

    private val period = 3_600

    @Test
    fun breathIsZeroAtStart() {
        assertEquals(0f, khotamBreath(0L, period), TOLERANCE)
    }

    @Test
    fun breathPeaksAtHalfPeriod() {
        assertEquals(1f, khotamBreath(period / 2L, period), TOLERANCE)
    }

    @Test
    fun breathWrapsAroundPeriod() {
        assertEquals(0f, khotamBreath(period.toLong(), period), TOLERANCE)
        assertEquals(khotamBreath(450L, period), khotamBreath(period * 3L + 450L, period), TOLERANCE)
    }

    @Test
    fun breathStaysWithinUnitRange() {
        (0L..period * 2L step 37L).forEach { elapsed ->
            val breath = khotamBreath(elapsed, period)
            assertTrue("breath $breath at $elapsed", breath in 0f..1f)
        }
    }

    @Test
    fun breathIsSymmetricAroundHalfPeriod() {
        listOf(100L, 600L, 1_200L, 1_700L).forEach { offset ->
            assertEquals(
                khotamBreath(period / 2L - offset, period),
                khotamBreath(period / 2L + offset, period),
                TOLERANCE,
            )
        }
    }

    private companion object {
        const val TOLERANCE = 1e-5f
    }
}
