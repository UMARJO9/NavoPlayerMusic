package tj.umar.navoplayer.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

private const val NANOS_PER_MILLI = 1_000_000L

@Composable
fun rememberPausableElapsedMillis(running: Boolean, frameIntervalMillis: Long = 0L): () -> Long {
    val elapsedNanos = rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(running, frameIntervalMillis) {
        if (!running) return@LaunchedEffect
        val motionScale = coroutineContext[MotionDurationScale]
        var lastFrame = withFrameNanos { it }
        while (coroutineContext.isActive) {
            if (motionScale != null && motionScale.scaleFactor == 0f) {
                snapshotFlow { motionScale.scaleFactor }.first { it > 0f }
                lastFrame = withFrameNanos { it }
            }
            val frame = withFrameNanos { it }
            elapsedNanos.longValue += frame - lastFrame
            lastFrame = frame
            if (frameIntervalMillis > 0L) delay(frameIntervalMillis)
        }
    }
    return remember(elapsedNanos) { { elapsedNanos.longValue / NANOS_PER_MILLI } }
}
