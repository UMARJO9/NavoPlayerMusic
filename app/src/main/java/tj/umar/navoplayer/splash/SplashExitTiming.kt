package tj.umar.navoplayer.splash

internal const val SPLASH_MAX_ICON_WAIT_MILLIS = 400L

internal fun splashExitStartDelayMillis(
    nowMillis: Long,
    iconAnimationStartMillis: Long,
    iconAnimationDurationMillis: Long,
    maxWaitMillis: Long = SPLASH_MAX_ICON_WAIT_MILLIS,
): Long {
    if (iconAnimationStartMillis == 0L || iconAnimationDurationMillis == 0L) return 0L
    val remaining = iconAnimationStartMillis + iconAnimationDurationMillis - nowMillis
    return remaining.coerceIn(0L, maxWaitMillis)
}

internal fun shouldAnimateSplashExit(animatorDurationScale: Float): Boolean = animatorDurationScale > 0f
