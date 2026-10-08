package tj.umar.navoplayer.core.designsystem.equalizer

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.R
import tj.umar.navoplayer.core.designsystem.animation.rememberPausableElapsedMillis
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private val BarWidth = 3.dp
private val BarGap = 3.dp
private val BarCorner = 1.5.dp
private val BarHeights = listOf(18.dp, 13.dp, 16.dp)
private val BarPhasesMillis = listOf(0L, 300L, 600L)
private const val HALF_CYCLE_MILLIS = 900L
internal const val MIN_SCALE = 0.35f
private val BarEasing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

@Composable
fun EqualizerBars(
    paused: Boolean,
    modifier: Modifier = Modifier,
    color: Color = NavoTheme.colors.accent,
) {
    val elapsed = rememberPausableElapsedMillis(running = !paused)
    val description = stringResource(R.string.designsystem_now_playing)
    Canvas(
        modifier = modifier
            .size(width = BarWidth * 3 + BarGap * 2, height = BarHeights.max())
            .semantics { contentDescription = description },
    ) {
        val now = elapsed()
        BarHeights.forEachIndexed { index, barHeight ->
            val fullHeight = barHeight.toPx()
            val barHeightPx = fullHeight * barScale(now + BarPhasesMillis[index])
            val left = index * (BarWidth + BarGap).toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(left, size.height - barHeightPx),
                size = Size(BarWidth.toPx(), barHeightPx),
                cornerRadius = CornerRadius(BarCorner.toPx()),
            )
        }
    }
}

internal fun barScale(timeMillis: Long): Float {
    val cycle = timeMillis % (HALF_CYCLE_MILLIS * 2)
    val progress = cycle.toFloat() / HALF_CYCLE_MILLIS
    val pingPong = if (progress > 1f) 2f - progress else progress
    return MIN_SCALE + (1f - MIN_SCALE) * BarEasing.transform(pingPong)
}

@Preview
@Composable
private fun EqualizerBarsPreview() {
    NavoTheme {
        Row(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            EqualizerBars(paused = false)
            EqualizerBars(paused = true)
        }
    }
}
