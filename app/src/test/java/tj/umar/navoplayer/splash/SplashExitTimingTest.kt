package tj.umar.navoplayer.splash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashExitTimingTest {

    @Test
    fun `running icon animation waits for the remaining time`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 1_700L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 900L,
        )
        assertEquals(200L, delay)
    }

    @Test
    fun `finished icon animation does not wait`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 3_000L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 900L,
        )
        assertEquals(0L, delay)
    }

    @Test
    fun `unknown start time does not wait`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 1_000L,
            iconAnimationStartMillis = 0L,
            iconAnimationDurationMillis = 900L,
        )
        assertEquals(0L, delay)
    }

    @Test
    fun `zero duration does not wait`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 1_000L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 0L,
        )
        assertEquals(0L, delay)
    }

    @Test
    fun `long remaining time is capped at the default maximum`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 1_100L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 900L,
        )
        assertEquals(SPLASH_MAX_ICON_WAIT_MILLIS, delay)
    }

    @Test
    fun `custom maximum caps the wait`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 1_100L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 900L,
            maxWaitMillis = 150L,
        )
        assertEquals(150L, delay)
    }

    @Test
    fun `now before animation start is capped`() {
        val delay = splashExitStartDelayMillis(
            nowMillis = 500L,
            iconAnimationStartMillis = 1_000L,
            iconAnimationDurationMillis = 900L,
        )
        assertEquals(SPLASH_MAX_ICON_WAIT_MILLIS, delay)
    }

    @Test
    fun `exit animates only when animator duration scale is positive`() {
        assertTrue(shouldAnimateSplashExit(1f))
        assertTrue(shouldAnimateSplashExit(0.5f))
        assertFalse(shouldAnimateSplashExit(0f))
    }
}
