package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tj.umar.navoplayer.core.designsystem.theme.NavoColors
import tj.umar.navoplayer.core.designsystem.theme.NavoTheme

private const val TINT_ALPHA = 0.8f
private const val PILL_TINT_ALPHA = 0.7f
private const val FALLBACK_ALPHA = 0.88f
private const val RIM_TOP_ALPHA = 0.20f
private const val RIM_BOTTOM_ALPHA = 0.04f
private const val DARK_SATURATION = 1.5f
private const val DARK_BRIGHTNESS = -0.06f
private const val LIGHT_SATURATION = 1.3f
private const val LIGHT_BRIGHTNESS = 0.04f
private const val DARK_HIGHLIGHT_ALPHA = 0.6f
private const val LIGHT_HIGHLIGHT_ALPHA = 0.85f
private const val DARK_SECONDARY_ALPHA = 0.9f
private val RimWidth = 1.dp

object NavoGlassDefaults {

    @Composable
    @ReadOnlyComposable
    fun bar(): NavoGlassStyle = barGlassStyle(NavoTheme.colors)

    @Composable
    @ReadOnlyComposable
    fun pill(): NavoGlassStyle = pillGlassStyle(NavoTheme.colors)

    @Composable
    @ReadOnlyComposable
    fun lens(): NavoGlassStyle = lensGlassStyle(NavoTheme.colors)
}

private const val LENS_TINT_ALPHA = 0.2f
private const val LENS_FALLBACK_ALPHA = 0.32f
private const val LENS_RIM_TOP_ALPHA = 0.4f
private const val LENS_RIM_BOTTOM_ALPHA = 0.08f
private const val LENS_SATURATION = 1.25f
private const val LENS_BRIGHTNESS = 0.06f
private const val LENS_HIGHLIGHT_ALPHA = 0.9f

internal fun lensGlassStyle(colors: NavoColors): NavoGlassStyle = NavoGlassStyle(
    blurRadius = 0.dp,
    refractionHeight = 14.dp,
    refractionAmount = 22.dp,
    chromaticAberration = true,
    saturation = LENS_SATURATION,
    brightness = LENS_BRIGHTNESS,
    tint = colors.accent.copy(alpha = LENS_TINT_ALPHA),
    fallbackTint = colors.accent.copy(alpha = LENS_FALLBACK_ALPHA),
    rimTop = colors.highlight.copy(alpha = LENS_RIM_TOP_ALPHA),
    rimBottom = colors.highlight.copy(alpha = LENS_RIM_BOTTOM_ALPHA),
    rimWidth = RimWidth,
    highlightAlpha = LENS_HIGHLIGHT_ALPHA,
    secondaryContent = glassSecondaryContent(colors),
)

internal fun barGlassStyle(colors: NavoColors): NavoGlassStyle {
    val surface = if (colors.isDark) colors.high else colors.card
    return glassStyle(
        colors = colors,
        surface = surface,
        tintAlpha = TINT_ALPHA,
        blurRadius = 12.dp,
        refractionHeight = 14.dp,
        refractionAmount = 28.dp,
    )
}

internal fun pillGlassStyle(colors: NavoColors): NavoGlassStyle {
    val surface = if (colors.isDark) colors.raised else colors.card
    return glassStyle(
        colors = colors,
        surface = surface,
        tintAlpha = PILL_TINT_ALPHA,
        blurRadius = 8.dp,
        refractionHeight = 12.dp,
        refractionAmount = 24.dp,
    )
}

internal fun glassSecondaryContent(colors: NavoColors): Color =
    if (colors.isDark) colors.content.copy(alpha = DARK_SECONDARY_ALPHA) else colors.contentMuted

private fun glassStyle(
    colors: NavoColors,
    surface: Color,
    tintAlpha: Float,
    blurRadius: Dp,
    refractionHeight: Dp,
    refractionAmount: Dp,
): NavoGlassStyle = NavoGlassStyle(
    blurRadius = blurRadius,
    refractionHeight = refractionHeight,
    refractionAmount = refractionAmount,
    chromaticAberration = false,
    saturation = if (colors.isDark) DARK_SATURATION else LIGHT_SATURATION,
    brightness = if (colors.isDark) DARK_BRIGHTNESS else LIGHT_BRIGHTNESS,
    tint = surface.copy(alpha = tintAlpha),
    fallbackTint = surface.copy(alpha = FALLBACK_ALPHA),
    rimTop = colors.highlight.copy(alpha = RIM_TOP_ALPHA),
    rimBottom = colors.highlight.copy(alpha = RIM_BOTTOM_ALPHA),
    rimWidth = RimWidth,
    highlightAlpha = if (colors.isDark) DARK_HIGHLIGHT_ALPHA else LIGHT_HIGHLIGHT_ALPHA,
    secondaryContent = glassSecondaryContent(colors),
)
