package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.medallion.Medallion
import tj.umar.navoplayer.core.designsystem.medallion.MedallionPalettes
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val RING_START_DEGREES = -90f
private const val FULL_TURN_DEGREES = 360f
private const val TRACK_ALPHA = 0.12f
private const val GLOW_ALPHA = 0.22f
private const val HALO_ALPHA = 0.28f
private const val MIN_VISIBLE_FRACTION = 0.004f
private const val GLOW_FADE_MILLIS = 400
private val GlowStrokeWidth = 6.dp
private val HaloRadius = 4.5.dp
private val BeadRadius = 2.dp

@Composable
fun ProgressRing(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    glowing: Boolean = true,
    strokeWidth: Dp = 2.5.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = NavoTheme.colors
    val glowAlpha = animateFloatAsState(
        targetValue = if (glowing) 1f else 0f,
        animationSpec = tween(GLOW_FADE_MILLIS),
        label = "progressRingGlow",
    )
    Box(
        modifier = modifier.drawWithCache {
            val stroke = strokeWidth.toPx()
            val glowStroke = GlowStrokeWidth.toPx()
            val haloRadius = HaloRadius.toPx()
            val beadRadius = BeadRadius.toPx()
            val reach = maxOf(stroke, glowStroke, haloRadius * 2f)
            val radius = (size.minDimension - reach) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)
            val trackColor = colors.content.copy(alpha = TRACK_ALPHA)
            val trackStroke = Stroke(stroke)
            val arcStroke = Stroke(stroke, cap = StrokeCap.Round)
            val glowArcStroke = Stroke(glowStroke, cap = StrokeCap.Round)
            onDrawBehind {
                val fraction = progress().coerceIn(0f, 1f)
                val glow = glowAlpha.value
                val sweep = fraction * FULL_TURN_DEGREES
                drawCircle(trackColor, radius = radius, center = center, style = trackStroke)
                if (fraction <= MIN_VISIBLE_FRACTION) return@onDrawBehind
                if (glow > 0f) {
                    drawArc(
                        color = colors.accent,
                        startAngle = RING_START_DEGREES,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        alpha = GLOW_ALPHA * glow,
                        style = glowArcStroke,
                    )
                }
                drawArc(
                    color = colors.accent,
                    startAngle = RING_START_DEGREES,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = arcStroke,
                )
                val head = ringHeadOffset(center, radius, fraction)
                if (glow > 0f) drawCircle(colors.accent, radius = haloRadius, center = head, alpha = HALO_ALPHA * glow)
                drawCircle(colors.content, radius = beadRadius, center = head)
            }
        },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

internal fun ringHeadOffset(center: Offset, radius: Float, fraction: Float): Offset {
    val angle = (RING_START_DEGREES + fraction * FULL_TURN_DEGREES) * PI / 180.0
    return Offset(
        x = center.x + radius * cos(angle).toFloat(),
        y = center.y + radius * sin(angle).toFloat(),
    )
}

@Preview
@Composable
private fun ProgressRingPreview() {
    NavoTheme {
        Row(
            modifier = Modifier
                .background(NavoTheme.colors.high)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            listOf(0f, 0.38f, 0.97f).forEach { fraction ->
                ProgressRing(progress = { fraction }, modifier = Modifier.size(58.dp)) {
                    Medallion(MedallionPalettes.all[0], Modifier.size(42.dp))
                }
            }
            ProgressRing(progress = { 0.6f }, modifier = Modifier.size(58.dp), glowing = false) {
                Medallion(MedallionPalettes.all[1], Modifier.size(42.dp))
            }
        }
    }
}
