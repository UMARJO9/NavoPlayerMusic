package tj.umar.navoplayer.core.designsystem.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight

private val UnitDensity = Density(1f)

fun Modifier.navoGlass(
    backdrop: NavoGlassBackdrop?,
    shape: Shape,
    style: NavoGlassStyle,
): Modifier {
    val tier = backdrop?.tier ?: GlassTier.Tint
    val glass = style.resolve(tier, UnitDensity)
    if (backdrop == null || tier == GlassTier.Tint) {
        return background(glass.tint, shape).glassRim(shape, style)
    }
    val highlight = glass.libraryHighlight?.let { alpha -> Highlight.Default.copy(alpha = alpha) }
    return drawBackdrop(
        backdrop = backdrop.layer,
        shape = { shape },
        effects = {
            val scaled = style.resolve(tier, this)
            scaled.colorControls?.let { controls ->
                colorControls(brightness = controls.brightness, saturation = controls.saturation)
            }
            if (scaled.blurPx > 0f) blur(scaled.blurPx)
            scaled.refraction?.fitTo(size)?.takeIf { it.heightPx > 0f }?.let { refraction ->
                lens(
                    refractionHeight = refraction.heightPx,
                    refractionAmount = refraction.amountPx,
                    chromaticAberration = refraction.chromaticAberration,
                )
            }
        },
        highlight = highlight?.let { { it } },
        shadow = null,
        onDrawSurface = { drawRect(glass.tint) },
    ).then(if (glass.drawRim) Modifier.glassRim(shape, style) else Modifier)
}

private fun Modifier.glassRim(shape: Shape, style: NavoGlassStyle): Modifier =
    border(style.rimWidth, Brush.verticalGradient(listOf(style.rimTop, style.rimBottom)), shape)
