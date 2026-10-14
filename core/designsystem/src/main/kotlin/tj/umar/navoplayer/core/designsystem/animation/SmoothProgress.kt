package tj.umar.navoplayer.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private data class ProgressSample(val positionMs: Long, val durationMs: Long, val playing: Boolean, val snap: Boolean)

@Composable
fun rememberSmoothProgress(
    positionMs: () -> Long,
    durationMs: () -> Long,
    playing: Boolean,
    frameIntervalMillis: Long = 0L,
    snap: Boolean = false,
): () -> Float {
    val interpolator = remember { ProgressInterpolator() }
    val shown = remember { mutableFloatStateOf(rawFraction(positionMs(), durationMs())) }
    val samples = remember { mutableIntStateOf(0) }
    val motionEnabled = rememberMotionEnabled()
    val currentPosition by rememberUpdatedState(positionMs)
    val currentDuration by rememberUpdatedState(durationMs)
    val currentPlaying by rememberUpdatedState(playing)
    val currentSnap by rememberUpdatedState(snap)
    val currentMotion by rememberUpdatedState(motionEnabled)

    LaunchedEffect(interpolator) {
        snapshotFlow { ProgressSample(currentPosition(), currentDuration(), currentPlaying, currentSnap) }
            .collect { sample ->
                withFrameMillis { now ->
                    if (sample.snap) {
                        interpolator.reset(sample.positionMs, sample.durationMs, sample.playing, now)
                    } else {
                        interpolator.onSample(sample.positionMs, sample.durationMs, sample.playing, now)
                    }
                    if (sample.snap || !currentMotion) {
                        shown.floatValue = rawFraction(sample.positionMs, sample.durationMs)
                    }
                }
                samples.intValue++
            }
    }

    LaunchedEffect(motionEnabled, frameIntervalMillis) {
        if (!motionEnabled) return@LaunchedEffect
        snapshotFlow { samples.intValue }.collectLatest {
            do {
                val now = withFrameMillis { frame ->
                    shown.floatValue = interpolator.fractionAt(frame)
                    frame
                }
                if (frameIntervalMillis > 0L) delay(frameIntervalMillis)
            } while (currentPlaying || interpolator.isSettling(now))
        }
    }

    return remember(shown) { { shown.floatValue } }
}

private fun rawFraction(positionMs: Long, durationMs: Long): Float =
    if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
