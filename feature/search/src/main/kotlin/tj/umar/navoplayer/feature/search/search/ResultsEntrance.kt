package tj.umar.navoplayer.feature.search.search

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import kotlin.math.min

internal const val ENTRANCE_ITEM_MILLIS = 240
internal const val ENTRANCE_STEP_MILLIS = 30
internal const val ENTRANCE_MAX_STAGGER = 8
internal const val ENTRANCE_TOTAL_MILLIS = ENTRANCE_MAX_STAGGER * ENTRANCE_STEP_MILLIS + ENTRANCE_ITEM_MILLIS

private const val ITEM_FADE_IN_MILLIS = 200
private const val ITEM_FADE_OUT_MILLIS = 120
private val EntranceOffset = 16.dp
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

internal fun entranceFraction(elapsedMillis: Float, index: Int): Float =
    ((elapsedMillis - min(index, ENTRANCE_MAX_STAGGER) * ENTRANCE_STEP_MILLIS) / ENTRANCE_ITEM_MILLIS).coerceIn(0f, 1f)

@Composable
internal fun rememberResultsEntrance(): () -> Float {
    val motion = rememberMotionEnabled()
    var played by rememberSaveable { mutableStateOf(false) }
    val elapsed = remember { Animatable(if (played || !motion) ENTRANCE_TOTAL_MILLIS.toFloat() else 0f) }
    LaunchedEffect(motion) {
        played = true
        if (motion) {
            elapsed.animateTo(
                ENTRANCE_TOTAL_MILLIS.toFloat(),
                tween(ENTRANCE_TOTAL_MILLIS - elapsed.value.toInt(), easing = LinearEasing),
            )
        } else {
            elapsed.snapTo(ENTRANCE_TOTAL_MILLIS.toFloat())
        }
    }
    return remember(elapsed) { { elapsed.value } }
}

internal fun Modifier.resultEntrance(index: Int, entrance: () -> Float): Modifier = graphicsLayer {
    val progress = EmphasizedDecelerate.transform(entranceFraction(entrance(), index))
    alpha = progress
    translationY = (1f - progress) * EntranceOffset.toPx()
}

internal fun Modifier.resultItem(
    scope: LazyItemScope,
    index: Int,
    motion: Boolean,
    entrance: () -> Float,
): Modifier = with(scope) {
    this@resultItem
        .animateItem(
            fadeInSpec = if (motion) tween(ITEM_FADE_IN_MILLIS) else null,
            placementSpec = if (motion) {
                spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)
            } else {
                null
            },
            fadeOutSpec = if (motion) tween(ITEM_FADE_OUT_MILLIS) else null,
        )
        .resultEntrance(index, entrance)
}
