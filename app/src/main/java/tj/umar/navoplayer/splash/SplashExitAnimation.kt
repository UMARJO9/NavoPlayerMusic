package tj.umar.navoplayer.splash

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.SystemClock
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreenViewProvider

private const val ICON_EXIT_DURATION_MILLIS = 380L
private const val BACKGROUND_EXIT_DURATION_MILLIS = 320L
private const val BACKGROUND_EXIT_DELAY_MILLIS = 120L
private const val ICON_EXIT_SCALE = 1.35f
private const val ICON_EXIT_ROTATION = 45f

internal fun SplashScreenViewProvider.playNavoExitAnimation(
    animatorDurationScale: Float,
    nowMillis: Long = SystemClock.uptimeMillis(),
    onRemoved: () -> Unit,
) {
    if (!shouldAnimateSplashExit(animatorDurationScale)) {
        remove()
        onRemoved()
        return
    }
    val exit = runCatching {
        AnimatorSet().apply {
            playTogether(iconExitAnimator(iconView), backgroundExitAnimator(view))
            startDelay = splashExitStartDelayMillis(
                nowMillis = nowMillis,
                iconAnimationStartMillis = iconAnimationStartMillis,
                iconAnimationDurationMillis = iconAnimationDurationMillis,
            )
        }
    }.getOrNull()
    if (exit == null) {
        remove()
        onRemoved()
        return
    }
    exit.apply {
        doOnEnd {
            remove()
            onRemoved()
        }
        start()
    }
}

private fun iconExitAnimator(icon: View): Animator =
    ObjectAnimator.ofPropertyValuesHolder(
        icon,
        PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, ICON_EXIT_SCALE),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, ICON_EXIT_SCALE),
        PropertyValuesHolder.ofFloat(View.ROTATION, 0f, ICON_EXIT_ROTATION),
        PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
    ).apply {
        duration = ICON_EXIT_DURATION_MILLIS
        interpolator = emphasizedInterpolator()
    }

private fun backgroundExitAnimator(view: View): Animator =
    ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
        duration = BACKGROUND_EXIT_DURATION_MILLIS
        startDelay = BACKGROUND_EXIT_DELAY_MILLIS
        interpolator = emphasizedInterpolator()
    }

private fun emphasizedInterpolator(): PathInterpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)
