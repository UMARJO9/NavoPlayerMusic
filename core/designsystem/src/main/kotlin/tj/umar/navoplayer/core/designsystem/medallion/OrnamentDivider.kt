package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val KHOTAM_POINTS = 8
private val StarGap = 8.dp
private val DotRadius = 1.5.dp
private val LineWidth = 1.dp

@Composable
fun KhotamStar(
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    color: Color = NavoTheme.colors.ornament,
) {
    Spacer(
        modifier = modifier
            .size(size)
            .drawWithCache {
                val radius = this.size.minDimension / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val star = starPath(starVertices(KHOTAM_POINTS, radius, khotamInnerRadius(radius), center))
                onDrawBehind { drawPath(star, color = color) }
            },
    )
}

@Composable
fun OrnamentDivider(
    modifier: Modifier = Modifier,
    color: Color = NavoTheme.colors.ornament,
    starSize: Dp = 12.dp,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(starSize)
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = starSize.toPx() / 2f
                val star = starPath(starVertices(KHOTAM_POINTS, radius, khotamInnerRadius(radius), center))
                val gap = radius + StarGap.toPx()
                val dotRadius = DotRadius.toPx()
                val lineWidth = LineWidth.toPx()
                val leftEnd = center.x - gap
                val rightStart = center.x + gap
                val leftBrush = Brush.horizontalGradient(listOf(Color.Transparent, color), startX = 0f, endX = leftEnd)
                val rightBrush = Brush.horizontalGradient(listOf(color, Color.Transparent), startX = rightStart, endX = size.width)
                onDrawBehind {
                    drawLine(leftBrush, Offset(0f, center.y), Offset(leftEnd - dotRadius * 3f, center.y), lineWidth)
                    drawLine(rightBrush, Offset(rightStart + dotRadius * 3f, center.y), Offset(size.width, center.y), lineWidth)
                    drawCircle(color, radius = dotRadius, center = Offset(leftEnd, center.y))
                    drawCircle(color, radius = dotRadius, center = Offset(rightStart, center.y))
                    drawPath(star, color = color)
                }
            },
    )
}

@Composable
private fun OrnamentDividerSample() {
    Column(
        modifier = Modifier
            .background(NavoTheme.colors.background)
            .padding(24.dp),
    ) {
        OrnamentDivider()
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KhotamStar()
            KhotamStar(size = 16.dp, color = NavoTheme.colors.accent)
        }
    }
}

@Preview(name = "Dark", widthDp = 360)
@Composable
private fun OrnamentDividerDarkPreview() {
    NavoTheme { OrnamentDividerSample() }
}

@Preview(name = "Light", widthDp = 360)
@Composable
private fun OrnamentDividerLightPreview() {
    NavoTheme(darkTheme = false) { OrnamentDividerSample() }
}
