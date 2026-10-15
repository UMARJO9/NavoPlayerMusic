package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val CHECK_REVEAL_MILLIS = 260
private const val CHECK_START_SCALE = 0.6f
private const val CHECK_POP_DAMPING = 0.5f
private val CheckRevealEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

@Composable
fun AnimatedCheck(
    visible: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = NavoTheme.colors.accent,
) {
    val motion = rememberMotionEnabled()
    val reveal = remember { Animatable(if (visible && !motion) 1f else 0f) }
    val scale = remember { Animatable(if (visible && !motion) 1f else CHECK_START_SCALE) }
    LaunchedEffect(visible, motion) {
        if (visible && motion) {
            coroutineScope {
                launch { reveal.animateTo(1f, tween(CHECK_REVEAL_MILLIS, easing = CheckRevealEasing)) }
                launch {
                    scale.animateTo(
                        1f,
                        spring(dampingRatio = CHECK_POP_DAMPING, stiffness = Spring.StiffnessMedium),
                    )
                }
            }
        } else if (visible) {
            reveal.snapTo(1f)
            scale.snapTo(1f)
        } else {
            reveal.snapTo(0f)
            scale.snapTo(CHECK_START_SCALE)
        }
    }
    Icon(
        imageVector = NavoIcons.Check,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .drawWithContent {
                clipRect(right = size.width * reveal.value) {
                    this@drawWithContent.drawContent()
                }
            },
    )
}

@Composable
private fun AnimatedCheckSample() {
    Row(
        modifier = Modifier
            .background(NavoTheme.colors.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AnimatedCheck(visible = true)
        AnimatedCheck(visible = true, tint = NavoTheme.colors.content)
    }
}

@Preview(name = "Dark")
@Composable
private fun AnimatedCheckDarkPreview() {
    NavoTheme { AnimatedCheckSample() }
}

@Preview(name = "Light")
@Composable
private fun AnimatedCheckLightPreview() {
    NavoTheme(darkTheme = false) { AnimatedCheckSample() }
}
