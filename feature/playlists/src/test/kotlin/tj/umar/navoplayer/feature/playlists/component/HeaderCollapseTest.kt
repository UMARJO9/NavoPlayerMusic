package tj.umar.navoplayer.feature.playlists.component

import org.junit.Assert.assertEquals
import org.junit.Test

class HeaderCollapseTest {

    @Test
    fun isExpandedAtTop() {
        assertEquals(0f, collapseFraction(firstVisibleIndex = 0, firstVisibleOffsetPx = 0, headerHeightPx = 400), TOLERANCE)
    }

    @Test
    fun isHalfCollapsedAtHalfOffset() {
        assertEquals(0.5f, collapseFraction(firstVisibleIndex = 0, firstVisibleOffsetPx = 200, headerHeightPx = 400), TOLERANCE)
    }

    @Test
    fun isFullyCollapsedWhenHeaderScrolledAway() {
        assertEquals(1f, collapseFraction(firstVisibleIndex = 1, firstVisibleOffsetPx = 0, headerHeightPx = 0), TOLERANCE)
        assertEquals(1f, collapseFraction(firstVisibleIndex = 3, firstVisibleOffsetPx = 50, headerHeightPx = 400), TOLERANCE)
    }

    @Test
    fun isExpandedWhenHeaderHeightIsUnknown() {
        assertEquals(0f, collapseFraction(firstVisibleIndex = 0, firstVisibleOffsetPx = 120, headerHeightPx = 0), TOLERANCE)
    }

    @Test
    fun isClampedToUnitRange() {
        assertEquals(1f, collapseFraction(firstVisibleIndex = 0, firstVisibleOffsetPx = 900, headerHeightPx = 400), TOLERANCE)
        assertEquals(0f, collapseFraction(firstVisibleIndex = 0, firstVisibleOffsetPx = -40, headerHeightPx = 400), TOLERANCE)
    }

    @Test
    fun collapsedTitleFadesInOverLastPartOfCollapse() {
        assertEquals(0f, collapsedTitleAlpha(0.6f), TOLERANCE)
        assertEquals(0.5f, collapsedTitleAlpha(0.8f), TOLERANCE)
        assertEquals(1f, collapsedTitleAlpha(1f), TOLERANCE)
        assertEquals(0f, collapsedTitleAlpha(0f), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 1e-5f
    }
}
