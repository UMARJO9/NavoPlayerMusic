package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

@Immutable
data class NavoGlassStyle(
    val blurRadius: Dp,
    val refractionHeight: Dp,
    val refractionAmount: Dp,
    val chromaticAberration: Boolean,
    val saturation: Float,
    val brightness: Float,
    val tint: Color,
    val fallbackTint: Color,
    val rimTop: Color,
    val rimBottom: Color,
    val rimWidth: Dp,
    val highlightAlpha: Float,
    val secondaryContent: Color,
)

@Immutable
internal data class GlassRefraction(
    val heightPx: Float,
    val amountPx: Float,
    val chromaticAberration: Boolean,
) {
    fun fitTo(size: Size): GlassRefraction {
        val limit = size.minDimension / 2f
        return if (heightPx <= limit) this else copy(heightPx = limit.coerceAtLeast(0f))
    }
}

@Immutable
internal data class GlassColorControls(val saturation: Float, val brightness: Float)

@Immutable
internal data class ResolvedGlass(
    val blurPx: Float,
    val refraction: GlassRefraction?,
    val colorControls: GlassColorControls?,
    val tint: Color,
    val libraryHighlight: Float?,
    val drawRim: Boolean,
)

internal fun NavoGlassStyle.resolve(tier: GlassTier, density: Density): ResolvedGlass = with(density) {
    when (tier) {
        GlassTier.Tint -> ResolvedGlass(
            blurPx = 0f,
            refraction = null,
            colorControls = null,
            tint = fallbackTint,
            libraryHighlight = null,
            drawRim = true,
        )
        GlassTier.Blur -> ResolvedGlass(
            blurPx = blurRadius.toPx(),
            refraction = null,
            colorControls = GlassColorControls(saturation, brightness),
            tint = tint,
            libraryHighlight = null,
            drawRim = true,
        )
        GlassTier.Refraction -> ResolvedGlass(
            blurPx = blurRadius.toPx(),
            refraction = GlassRefraction(
                heightPx = refractionHeight.toPx(),
                amountPx = refractionAmount.toPx(),
                chromaticAberration = chromaticAberration,
            ).takeIf { it.heightPx > 0f && it.amountPx > 0f },
            colorControls = GlassColorControls(saturation, brightness),
            tint = tint,
            libraryHighlight = highlightAlpha.takeIf { it > 0f },
            drawRim = highlightAlpha <= 0f,
        )
    }
}
