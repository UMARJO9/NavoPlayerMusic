package tj.umar.navoplayer.core.testing.time

import tj.umar.navoplayer.core.common.time.NavoClock

class FakeNavoClock(var now: Long = 0L) : NavoClock {

    override fun nowMillis(): Long = now

    fun advanceBy(millis: Long) {
        now += millis
    }
}
