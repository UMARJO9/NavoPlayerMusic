package tj.umar.navoplayer.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

private const val SHARED_AXIS_MILLIS = 320
private const val SHARED_AXIS_FADE_IN_MILLIS = 220
private const val SHARED_AXIS_FADE_IN_DELAY_MILLIS = 90
private const val SHARED_AXIS_FADE_OUT_MILLIS = 120
private const val SHARED_AXIS_OFFSET_FRACTION = 0.3f
private const val FADE_THROUGH_IN_MILLIS = 210
private const val FADE_THROUGH_OUT_MILLIS = 90
private const val FADE_THROUGH_SCALE = 0.92f

private fun slideSpec() = tween<IntOffset>(SHARED_AXIS_MILLIS, easing = FastOutSlowInEasing)

private fun offset(width: Int): Int = (width * SHARED_AXIS_OFFSET_FRACTION).toInt()

internal fun sharedAxisForwardEnter(): EnterTransition =
    slideInHorizontally(slideSpec()) { width -> offset(width) } +
        fadeIn(tween(SHARED_AXIS_FADE_IN_MILLIS, delayMillis = SHARED_AXIS_FADE_IN_DELAY_MILLIS))

internal fun sharedAxisForwardExit(): ExitTransition =
    slideOutHorizontally(slideSpec()) { width -> -offset(width) } +
        fadeOut(tween(SHARED_AXIS_FADE_OUT_MILLIS))

internal fun sharedAxisBackwardEnter(): EnterTransition =
    slideInHorizontally(slideSpec()) { width -> -offset(width) } +
        fadeIn(tween(SHARED_AXIS_MILLIS))

internal fun sharedAxisBackwardExit(): ExitTransition =
    slideOutHorizontally(slideSpec()) { width -> offset(width) } +
        fadeOut(tween(SHARED_AXIS_MILLIS))

internal fun fadeThroughEnter(): EnterTransition =
    fadeIn(tween(FADE_THROUGH_IN_MILLIS, delayMillis = FADE_THROUGH_OUT_MILLIS)) +
        scaleIn(tween(FADE_THROUGH_IN_MILLIS, delayMillis = FADE_THROUGH_OUT_MILLIS), initialScale = FADE_THROUGH_SCALE)

internal fun fadeThroughExit(): ExitTransition = fadeOut(tween(FADE_THROUGH_OUT_MILLIS))
