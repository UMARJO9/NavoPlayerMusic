package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.animation.rememberPausableElapsedMillis
import tj.umar.navoplayer.core.designsystem.theme.Ivory
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

enum class MedallionVariant { Simple, Detailed }

private val DetailedMinSize = 120.dp
private const val PETAL_COUNT = 8
private const val PETAL_STEP_DEGREES = 45f
private const val PETAL_OFFSET_DEGREES = 22.5f

@Composable
fun Medallion(
    palette: MedallionPalette,
    modifier: Modifier = Modifier,
    variant: MedallionVariant? = null,
    rotationDegrees: () -> Float = { 0f },
) {
    Spacer(
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees() }
            .drawWithCache {
                val resolved = variant ?: if (size.minDimension < DetailedMinSize.toPx()) {
                    MedallionVariant.Simple
                } else {
                    MedallionVariant.Detailed
                }
                onDrawBehind {
                    when (resolved) {
                        MedallionVariant.Simple -> drawSimpleMedallion(palette)
                        MedallionVariant.Detailed -> drawDetailedMedallion(palette)
                    }
                }
            },
    )
}

@Composable
fun rememberMedallionRotation(
    running: Boolean,
    periodMillis: Int = 40_000,
    clockwise: Boolean = true,
): () -> Float {
    val elapsed = rememberPausableElapsedMillis(running)
    val direction = if (clockwise) 1f else -1f
    return remember(elapsed, periodMillis, direction) {
        { direction * (elapsed() % periodMillis).toFloat() / periodMillis * 360f }
    }
}

internal fun DrawScope.drawSimpleMedallion(palette: MedallionPalette) {
    val scale = size.minDimension / 100f
    val center = Offset(size.width / 2f, size.height / 2f)
    drawCircle(palette.background, radius = 50f * scale, center = center)
    repeat(PETAL_COUNT) { index ->
        rotate(index * PETAL_STEP_DEGREES, pivot = center) {
            drawOval(
                color = palette.petals,
                topLeft = center + Offset(-10f * scale, -46f * scale),
                size = Size(20f * scale, 40f * scale),
            )
        }
    }
    drawCircle(palette.center, radius = 17f * scale, center = center)
    drawCircle(palette.background, radius = 7f * scale, center = center)
}

internal fun DrawScope.drawDetailedMedallion(palette: MedallionPalette) {
    val scale = size.minDimension / 200f
    val center = Offset(size.width / 2f, size.height / 2f)
    drawCircle(palette.background, radius = 100f * scale, center = center)
    drawCircle(
        color = palette.petals,
        radius = 91f * scale,
        center = center,
        style = Stroke(
            width = 2f * scale,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.5f * scale, 6f * scale)),
        ),
    )
    repeat(PETAL_COUNT) { index ->
        rotate(PETAL_OFFSET_DEGREES + index * PETAL_STEP_DEGREES, pivot = center) {
            drawOval(
                color = palette.center,
                topLeft = center + Offset(-4.5f * scale, -60f * scale),
                size = Size(9f * scale, 36f * scale),
            )
        }
    }
    repeat(PETAL_COUNT) { index ->
        rotate(index * PETAL_STEP_DEGREES, pivot = center) {
            drawOval(
                color = palette.petals,
                topLeft = center + Offset(-15f * scale, -82f * scale),
                size = Size(30f * scale, 64f * scale),
            )
        }
    }
    repeat(PETAL_COUNT) { index ->
        rotate(PETAL_OFFSET_DEGREES + index * PETAL_STEP_DEGREES, pivot = center) {
            drawCircle(Ivory, radius = 4f * scale, center = center + Offset(0f, -83f * scale))
        }
    }
    drawCircle(palette.center, radius = 32f * scale, center = center)
    drawCircle(palette.petals, radius = 24f * scale, center = center, style = Stroke(width = 3f * scale))
    drawCircle(palette.background, radius = 11f * scale, center = center)
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(widthDp = 390, heightDp = 400)
@Composable
private fun MedallionSimplePalettesPreview() {
    NavoTheme {
        FlowRow(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MedallionPalettes.all.forEach { Medallion(it, Modifier.size(48.dp)) }
            MedallionPalettes.all.forEach { Medallion(it, Modifier.size(104.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(widthDp = 480, heightDp = 1000)
@Composable
private fun MedallionDetailedPalettesPreview() {
    NavoTheme {
        FlowRow(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MedallionPalettes.all.forEach { Medallion(it, Modifier.size(220.dp)) }
        }
    }
}
