package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressRingTest {

    private val center = Offset(50f, 50f)
    private val radius = 20f

    @Test
    fun startsAtTopCenter() {
        assertOffset(Offset(50f, 30f), ringHeadOffset(center, radius, 0f))
    }

    @Test
    fun quarterIsOnTheRight() {
        assertOffset(Offset(70f, 50f), ringHeadOffset(center, radius, 0.25f))
    }

    @Test
    fun halfIsAtBottom() {
        assertOffset(Offset(50f, 70f), ringHeadOffset(center, radius, 0.5f))
    }

    @Test
    fun fullReturnsToTopCenter() {
        assertOffset(Offset(50f, 30f), ringHeadOffset(center, radius, 1f))
    }

    private fun assertOffset(expected: Offset, actual: Offset) {
        assertEquals(expected.x, actual.x, DELTA)
        assertEquals(expected.y, actual.y, DELTA)
    }

    private companion object {
        const val DELTA = 0.01f
    }
}
