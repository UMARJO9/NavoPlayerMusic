package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberMotionEnabled
import tj.umar.navoplayer.core.designsystem.animation.rememberPausableElapsedMillis
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import kotlin.math.PI
import kotlin.math.cos

private const val HALO_POINTS = 8
private const val HALO_BOX_SCALE = 1.6f
private const val HALO_MAX_SCALE = 1.04f
private const val HALO_MIN_SCALE = 0.94f
private const val HALO_SCALE_RANGE = 0.10f
private const val HALO_MIN_ALPHA = 0.10f
private const val HALO_ALPHA_RANGE = 0.12f
private const val HALO_INNER_RADIUS_SCALE = 0.82f
private const val HALO_INNER_ALPHA = 0.6f
private const val HALO_STILL_BREATH = 0.5f
private const val CLOCK_FRAME_MILLIS = 32L
private const val ENTRY_START_SCALE = 0.92f
private const val ENTRY_MILLIS = 420
private val EntryEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val HaloStrokeWidth = 1.dp

internal fun khotamBreath(elapsedMillis: Long, periodMillis: Int): Float {
    val phase = (elapsedMillis % periodMillis).toDouble() / periodMillis
    return ((1.0 - cos(2.0 * PI * phase)) / 2.0).toFloat()
}

@Composable
fun KhotamHaloMedallion(
    palette: MedallionPalette,
    modifier: Modifier = Modifier,
    medallionSize: Dp = 104.dp,
    rotationPeriodMillis: Int = 60_000,
    breathPeriodMillis: Int = 3_600,
    haloColor: Color = NavoTheme.colors.ornament,
) {
    val motion = rememberMotionEnabled()
    val elapsed = rememberPausableElapsedMillis(running = motion, frameIntervalMillis = CLOCK_FRAME_MILLIS)
    val entry = remember { Animatable(ENTRY_START_SCALE) }
    LaunchedEffect(motion) {
        if (motion) {
            entry.animateTo(1f, tween(ENTRY_MILLIS, easing = EntryEasing))
        } else {
            entry.snapTo(1f)
        }
    }
    Box(
        modifier = modifier
            .size(medallionSize * HALO_BOX_SCALE)
            .graphicsLayer {
                val scale = entry.value
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val strokeWidth = HaloStrokeWidth.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = size.minDimension / 2f / HALO_MAX_SCALE - strokeWidth
                    val innerRadius = outerRadius * HALO_INNER_RADIUS_SCALE
                    val outer = starPath(starVertices(HALO_POINTS, outerRadius, khotamInnerRadius(outerRadius), center))
                    val inner = starPath(starVertices(HALO_POINTS, innerRadius, khotamInnerRadius(innerRadius), center))
                    val stroke = Stroke(strokeWidth)
                    onDrawBehind {
                        val breath = if (motion) khotamBreath(elapsed(), breathPeriodMillis) else HALO_STILL_BREATH
                        val alpha = HALO_MIN_ALPHA + HALO_ALPHA_RANGE * breath
                        scale(HALO_MIN_SCALE + HALO_SCALE_RANGE * breath, pivot = center) {
                            drawPath(outer, color = haloColor.copy(alpha = alpha), style = stroke)
                            drawPath(inner, color = haloColor.copy(alpha = alpha * HALO_INNER_ALPHA), style = stroke)
                        }
                    }
                },
        )
        Medallion(
            palette = palette,
            modifier = Modifier.size(medallionSize),
            rotationDegrees = {
                if (motion) (elapsed() % rotationPeriodMillis).toFloat() / rotationPeriodMillis * 360f else 0f
            },
        )
    }
}

@Composable
private fun KhotamHaloMedallionSample() {
    Box(
        modifier = Modifier
            .background(NavoTheme.colors.background)
            .padding(16.dp),
    ) {
        KhotamHaloMedallion(palette = MedallionPalettes.all[2])
    }
}

@Preview
@Composable
private fun KhotamHaloMedallionPreview() {
    NavoTheme { KhotamHaloMedallionSample() }
}

@Preview
@Composable
private fun KhotamHaloMedallionLightPreview() {
    NavoTheme(darkTheme = false) { KhotamHaloMedallionSample() }
}
