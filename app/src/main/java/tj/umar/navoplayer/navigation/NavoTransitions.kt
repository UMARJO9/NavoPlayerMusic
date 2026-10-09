package tj.umar.navoplayer.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

private const val SHARED_AXIS_MILLIS = 320
private const val SHARED_AXIS_FADE_IN_MILLIS = 200
private const val SHARED_AXIS_FADE_OUT_MILLIS = 120
private const val SHARED_AXIS_OFFSET_FRACTION = 0.3f

private fun slideSpec() = tween<IntOffset>(SHARED_AXIS_MILLIS, easing = FastOutSlowInEasing)

private fun offset(width: Int): Int = (width * SHARED_AXIS_OFFSET_FRACTION).toInt()

internal fun sharedAxisForwardEnter(): EnterTransition =
    slideInHorizontally(slideSpec()) { width -> offset(width) } +
        fadeIn(tween(SHARED_AXIS_FADE_IN_MILLIS, delayMillis = SHARED_AXIS_FADE_OUT_MILLIS))

internal fun sharedAxisForwardExit(): ExitTransition =
    slideOutHorizontally(slideSpec()) { width -> -offset(width) } +
        fadeOut(tween(SHARED_AXIS_FADE_OUT_MILLIS))

internal fun sharedAxisBackwardEnter(): EnterTransition =
    slideInHorizontally(slideSpec()) { width -> -offset(width) } +
        fadeIn(tween(SHARED_AXIS_FADE_IN_MILLIS, delayMillis = SHARED_AXIS_FADE_OUT_MILLIS))

internal fun sharedAxisBackwardExit(): ExitTransition =
    slideOutHorizontally(slideSpec()) { width -> offset(width) } +
        fadeOut(tween(SHARED_AXIS_FADE_OUT_MILLIS))
