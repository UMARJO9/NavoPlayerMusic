package tj.umar.navoplayer.core.designsystem.animation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.MotionDurationScale

@Composable
fun rememberMotionEnabled(): Boolean {
    var enabled by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val motionScale = coroutineContext[MotionDurationScale] ?: return@LaunchedEffect
        snapshotFlow { motionScale.scaleFactor > 0f }.collect { enabled = it }
    }
    return enabled
}
