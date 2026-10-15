package tj.umar.navoplayer.feature.search.search

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultsEntranceTest {

    @Test
    fun firstItemStartsHiddenAndFinishesAfterItemDuration() {
        assertEquals(0f, entranceFraction(0f, 0), TOLERANCE)
        assertEquals(0.5f, entranceFraction(ENTRANCE_ITEM_MILLIS / 2f, 0), TOLERANCE)
        assertEquals(1f, entranceFraction(ENTRANCE_ITEM_MILLIS.toFloat(), 0), TOLERANCE)
    }

    @Test
    fun laterItemsAreDelayedByStep() {
        val index = 3
        val start = index * ENTRANCE_STEP_MILLIS.toFloat()
        assertEquals(0f, entranceFraction(start, index), TOLERANCE)
        assertEquals(1f, entranceFraction(start + ENTRANCE_ITEM_MILLIS, index), TOLERANCE)
    }

    @Test
    fun staggerIsCappedAtMax() {
        val elapsed = ENTRANCE_MAX_STAGGER * ENTRANCE_STEP_MILLIS + ENTRANCE_ITEM_MILLIS / 2f
        assertEquals(entranceFraction(elapsed, ENTRANCE_MAX_STAGGER), entranceFraction(elapsed, 40), TOLERANCE)
    }

    @Test
    fun everyItemIsVisibleAtTotal() {
        (0..50).forEach { index ->
            assertEquals(1f, entranceFraction(ENTRANCE_TOTAL_MILLIS.toFloat(), index), TOLERANCE)
        }
    }

    @Test
    fun fractionIsClampedToUnitRange() {
        assertEquals(0f, entranceFraction(-100f, 0), TOLERANCE)
        assertEquals(1f, entranceFraction(10_000f, 2), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-5f
    }
}
