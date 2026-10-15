package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.icon.NavoIcons
import tj.umar.navoplayer.core.designsystem.theme.LapisNavoColors
import tj.umar.navoplayer.core.designsystem.theme.NavoColors
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme
import tj.umar.navoplayer.core.designsystem.theme.ParchmentNavoColors

enum class ThemeSwatchVariant { Dark, Light, Split }

private val ThemeSwatchShape = RoundedCornerShape(16.dp)
private val SelectedBorderWidth = 2.dp
private val UnselectedBorderWidth = 1.dp
private val CheckBadgeSize = 20.dp
private val CheckIconSize = 14.dp
private const val THEME_SWATCH_ASPECT_RATIO = 0.78f

@Composable
fun ThemeSwatch(
    variant: ThemeSwatchVariant,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = NavoTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(THEME_SWATCH_ASPECT_RATIO)
            .clip(ThemeSwatchShape)
            .border(
                width = if (selected) SelectedBorderWidth else UnselectedBorderWidth,
                color = if (selected) colors.accent else colors.line,
                shape = ThemeSwatchShape,
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (variant) {
                ThemeSwatchVariant.Dark -> drawMiniScreen(LapisNavoColors)
                ThemeSwatchVariant.Light -> drawMiniScreen(ParchmentNavoColors)
                ThemeSwatchVariant.Split -> {
                    clipPath(splitPath(size, darkHalf = true)) { drawMiniScreen(LapisNavoColors) }
                    clipPath(splitPath(size, darkHalf = false)) { drawMiniScreen(ParchmentNavoColors) }
                }
            }
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(CheckBadgeSize)
                    .background(colors.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = NavoIcons.Check,
                    contentDescription = null,
                    tint = colors.onAccent,
                    modifier = Modifier.size(CheckIconSize),
                )
            }
        }
    }
}

private fun splitPath(size: Size, darkHalf: Boolean): Path = Path().apply {
    if (darkHalf) {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(0f, size.height)
    } else {
        moveTo(size.width, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
    }
    close()
}

private fun DrawScope.drawMiniScreen(colors: NavoColors) {
    val width = size.width
    val height = size.height
    val inset = width * 0.12f
    val lineHeight = height * 0.045f
    val lineRadius = CornerRadius(lineHeight / 2f)
    drawRect(colors.background)
    drawCircle(colors.accent, radius = width * 0.07f, center = Offset(inset + width * 0.07f, height * 0.14f))
    drawRoundRect(
        color = colors.content,
        topLeft = Offset(inset + width * 0.2f, height * 0.14f - lineHeight / 2f),
        size = Size(width * 0.4f, lineHeight),
        cornerRadius = lineRadius,
    )
    val cardTop = height * 0.27f
    val cardHeight = height * 0.38f
    drawRoundRect(
        color = colors.card,
        topLeft = Offset(inset, cardTop),
        size = Size(width - inset * 2f, cardHeight),
        cornerRadius = CornerRadius(width * 0.08f),
    )
    val rowTiles = listOf(colors.turquoise, colors.crimson)
    rowTiles.forEachIndexed { index, tileColor ->
        val rowCenterY = cardTop + cardHeight * (0.28f + index * 0.44f)
        drawCircle(tileColor, radius = width * 0.055f, center = Offset(inset + width * 0.12f, rowCenterY))
        drawRoundRect(
            color = colors.contentSecondary,
            topLeft = Offset(inset + width * 0.22f, rowCenterY - lineHeight / 2f),
            size = Size(width * 0.36f, lineHeight),
            cornerRadius = lineRadius,
        )
    }
    val playerTop = height * 0.76f
    val playerHeight = height * 0.13f
    drawRoundRect(
        color = colors.raised,
        topLeft = Offset(inset, playerTop),
        size = Size(width - inset * 2f, playerHeight),
        cornerRadius = CornerRadius(playerHeight / 2f),
    )
    drawCircle(
        color = colors.accent,
        radius = playerHeight * 0.3f,
        center = Offset(width - inset - playerHeight / 2f, playerTop + playerHeight / 2f),
    )
}

@Composable
private fun ThemeSwatchSample() {
    Row(
        modifier = Modifier
            .background(NavoTheme.colors.background)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ThemeSwatch(variant = ThemeSwatchVariant.Split, selected = false, modifier = Modifier.width(96.dp))
        ThemeSwatch(variant = ThemeSwatchVariant.Light, selected = false, modifier = Modifier.width(96.dp))
        ThemeSwatch(variant = ThemeSwatchVariant.Dark, selected = true, modifier = Modifier.width(96.dp))
    }
}

@Preview(name = "Dark")
@Composable
private fun ThemeSwatchDarkPreview() {
    NavoTheme { ThemeSwatchSample() }
}

@Preview(name = "Light")
@Composable
private fun ThemeSwatchLightPreview() {
    NavoTheme(darkTheme = false) { ThemeSwatchSample() }
}
