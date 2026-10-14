package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val KHOTAM_POINTS = 8
private const val INNER_STAR_SCALE = 0.62f
private const val INNER_STAR_ALPHA = 0.6f

fun Modifier.khotamWatermark(
    color: Color,
    diameter: Dp = 112.dp,
    endOffset: Dp = 36.dp,
    strokeWidth: Dp = 1.dp,
): Modifier = drawWithCache {
    val center = Offset(size.width - endOffset.toPx(), size.height / 2f)
    val outerRadius = diameter.toPx() / 2f
    val innerRadius = outerRadius * INNER_STAR_SCALE
    val outer = starPath(starVertices(KHOTAM_POINTS, outerRadius, khotamInnerRadius(outerRadius), center))
    val inner = starPath(starVertices(KHOTAM_POINTS, innerRadius, khotamInnerRadius(innerRadius), center))
    val stroke = Stroke(strokeWidth.toPx())
    val innerColor = color.copy(alpha = color.alpha * INNER_STAR_ALPHA)
    onDrawBehind {
        drawPath(outer, color = color, style = stroke)
        drawPath(inner, color = innerColor, style = stroke)
    }
}

@Preview(widthDp = 390, heightDp = 100)
@Composable
private fun KhotamWatermarkPreview() {
    NavoTheme {
        Box(
            modifier = Modifier
                .background(NavoTheme.colors.background)
                .padding(12.dp)
                .fillMaxWidth()
                .height(68.dp)
                .background(NavoTheme.colors.high)
                .khotamWatermark(NavoTheme.colors.content.copy(alpha = 0.06f)),
        )
    }
}
