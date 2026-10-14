package tj.umar.navoplayer.core.designsystem.animation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressInterpolatorTest {

    private val interpolator = ProgressInterpolator()

    @Test
    fun extrapolatesLinearlyWhilePlaying() {
        interpolator.onSample(10_000, 200_000, playing = true, nowMillis = 0)

        assertEquals(10_250f, interpolator.positionAt(250), DELTA)
    }

    @Test
    fun holdsPositionWhilePaused() {
        interpolator.onSample(10_000, 200_000, playing = false, nowMillis = 0)

        assertEquals(10_000f, interpolator.positionAt(800), DELTA)
    }

    @Test
    fun capsExtrapolationWhenSamplesStop() {
        interpolator.onSample(10_000, 200_000, playing = true, nowMillis = 0)

        assertEquals(11_000f, interpolator.positionAt(5_000), DELTA)
    }

    @Test
    fun smallLagWhilePlayingNeverMovesBackwards() {
        interpolator.onSample(10_000, 200_000, playing = true, nowMillis = 0)
        val before = interpolator.positionAt(500)

        interpolator.onSample(10_400, 200_000, playing = true, nowMillis = 500)

        assertTrue(interpolator.positionAt(500) >= before)
        assertTrue(interpolator.positionAt(550) >= before)
    }

    @Test
    fun smallDriftConvergesToSample() {
        interpolator.onSample(10_000, 200_000, playing = false, nowMillis = 0)
        interpolator.onSample(10_300, 200_000, playing = false, nowMillis = 100)

        assertTrue(interpolator.isSettling(300))
        assertEquals(10_300f, interpolator.positionAt(500), DELTA)
        assertTrue(!interpolator.isSettling(500))
    }

    @Test
    fun seekEasesToTarget() {
        interpolator.onSample(10_000, 200_000, playing = false, nowMillis = 0)
        interpolator.onSample(100_000, 200_000, playing = false, nowMillis = 1_000)

        val midway = interpolator.positionAt(1_120)
        assertTrue(midway > 10_000f && midway < 100_000f)
        assertEquals(100_000f, interpolator.positionAt(1_240), DELTA)
    }

    @Test
    fun durationChangeRewindsTowardNewPosition() {
        interpolator.onSample(190_000, 200_000, playing = true, nowMillis = 0)
        interpolator.onSample(0, 120_000, playing = true, nowMillis = 500)

        val midway = interpolator.positionAt(600)
        assertTrue(midway < 120_000f)
        assertEquals(240f, interpolator.positionAt(740), DELTA)
    }

    @Test
    fun zeroDurationGivesZeroFraction() {
        interpolator.onSample(5_000, 0, playing = true, nowMillis = 0)

        assertEquals(0f, interpolator.fractionAt(100), DELTA)
    }

    @Test
    fun clampsToDuration() {
        interpolator.onSample(199_900, 200_000, playing = true, nowMillis = 0)

        assertEquals(200_000f, interpolator.positionAt(900), DELTA)
        assertEquals(1f, interpolator.fractionAt(900), DELTA)
    }

    private companion object {
        const val DELTA = 0.5f
    }
}
