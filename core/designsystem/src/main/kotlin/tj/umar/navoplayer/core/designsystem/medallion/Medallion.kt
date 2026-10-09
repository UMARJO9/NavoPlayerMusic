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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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
                when (resolved) {
                    MedallionVariant.Simple -> {
                        val star = simpleKhotamStar(size)
                        onDrawBehind { drawSimpleMedallion(palette, star) }
                    }
                    MedallionVariant.Detailed -> {
                        val ornament = SuzaniOrnament(size)
                        onDrawBehind { drawSuzaniMedallion(palette, ornament, rotationDegrees()) }
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

internal fun simpleKhotamStar(size: Size): Path {
    val unit = size.minDimension / 100f
    val center = Offset(size.width / 2f, size.height / 2f)
    return starPath(
        starVertices(
            points = PETAL_COUNT,
            outerRadius = 46f * unit,
            innerRadius = khotamInnerRadius(46f * unit),
            center = center,
        ),
    )
}

internal fun DrawScope.drawSimpleMedallion(palette: MedallionPalette, star: Path = simpleKhotamStar(size)) {
    val unit = size.minDimension / 100f
    val center = Offset(size.width / 2f, size.height / 2f)
    drawCircle(palette.background, radius = 50f * unit, center = center)
    drawPath(star, color = palette.petals)
    drawCircle(palette.center, radius = 22f * unit, center = center)
    repeat(PETAL_COUNT) { index ->
        rotate(index * PETAL_STEP_DEGREES, pivot = center) {
            drawOval(
                color = palette.petals,
                topLeft = center + Offset(-3.5f * unit, -19f * unit),
                size = Size(7f * unit, 12f * unit),
            )
        }
    }
    drawCircle(Ivory, radius = 5f * unit, center = center)
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
