package tj.umar.navoplayer.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

private const val NANOS_PER_MILLI = 1_000_000L

@Composable
fun rememberPausableElapsedMillis(running: Boolean): () -> Long {
    val elapsedNanos = rememberSaveable { mutableLongStateOf(0L) }
    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        var lastFrame = withFrameNanos { it }
        while (coroutineContext.isActive) {
            val frame = withFrameNanos { it }
            val scale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f
            if (scale > 0f) elapsedNanos.longValue += frame - lastFrame
            lastFrame = frame
        }
    }
    return { elapsedNanos.longValue / NANOS_PER_MILLI }
}
