package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.sin

private const val UNITS = 200f
private const val BAND_MOTIFS = 16
private const val BAND_STEP_DEGREES = 360f / BAND_MOTIFS
private const val STAR_POINTS = 8
private const val STAR_STEP_DEGREES = 360f / STAR_POINTS
private const val ROSETTE_PETALS = 12
private const val ROSETTE_STEP_DEGREES = 360f / ROSETTE_PETALS
private const val STAR_LAYER_TURN = -2f
private const val ROSETTE_LAYER_TURN = 0.5f
private const val BREATH_CYCLES_PER_TURN = 10f
private const val BREATH_DEPTH = 0.06f
private const val DEGREES_TO_RADIANS = (PI / 180).toFloat()

internal class SuzaniOrnament(size: Size) {
    val unit: Float = size.minDimension / UNITS
    val center: Offset = Offset(size.width / 2f, size.height / 2f)
    val star: Path = starPath(
        starVertices(
            points = STAR_POINTS,
            outerRadius = 68f * unit,
            innerRadius = khotamInnerRadius(68f * unit),
            center = center,
        ),
    )
    val tulip: Path = tulipPath(base = center + Offset(0f, -20f * unit), length = 38f * unit, width = 12f * unit)
    val bodom: Path = bodomPath(center = center + Offset(0f, -84f * unit), length = 15f * unit, width = 7f * unit)
    val starStroke: Stroke = Stroke(width = 2f * unit)
    val outerRingStroke: Stroke = Stroke(width = 1.5f * unit)
    val innerRingStroke: Stroke = Stroke(width = 2f * unit)
}

internal fun breathScale(turnDegrees: Float): Float =
    1f + BREATH_DEPTH * sin(turnDegrees * BREATH_CYCLES_PER_TURN * DEGREES_TO_RADIANS)

internal fun DrawScope.drawSuzaniMedallion(
    palette: MedallionPalette,
    ornament: SuzaniOrnament = SuzaniOrnament(size),
    turnDegrees: Float = 0f,
) {
    val unit = ornament.unit
    val center = ornament.center
    drawCircle(palette.background, radius = 100f * unit, center = center)
    drawOuterBand(palette, ornament)
    val starDot = ivoryOn(palette.center, palette.background)
    rotate(STAR_LAYER_TURN * turnDegrees, pivot = center) {
        drawPath(ornament.star, color = palette.center)
        drawPath(ornament.star, color = palette.petals, style = ornament.starStroke)
        repeat(STAR_POINTS) { index ->
            rotate(index * STAR_STEP_DEGREES, pivot = center) {
                drawPath(ornament.tulip, color = palette.petals)
                drawCircle(starDot, radius = 2.5f * unit, center = center + Offset(0f, -62f * unit))
            }
        }
    }
    val breath = breathScale(turnDegrees)
    rotate(ROSETTE_LAYER_TURN * turnDegrees, pivot = center) {
        scale(breath, pivot = center) {
            drawCircle(palette.background, radius = 26f * unit, center = center)
            repeat(ROSETTE_PETALS) { index ->
                rotate(index * ROSETTE_STEP_DEGREES, pivot = center) {
                    drawOval(
                        color = palette.petals,
                        topLeft = center + Offset(-3f * unit, -24f * unit),
                        size = Size(6f * unit, 13f * unit),
                    )
                }
            }
            drawCircle(palette.center, radius = 9f * unit, center = center)
            drawCircle(ivoryOn(palette.center, palette.background), radius = 3.5f * unit, center = center)
        }
    }
}

private fun DrawScope.drawOuterBand(palette: MedallionPalette, ornament: SuzaniOrnament) {
    val bandDot = ivoryOn(palette.background, palette.center)
    val unit = ornament.unit
    val center = ornament.center
    drawCircle(palette.petals, radius = 96f * unit, center = center, style = ornament.outerRingStroke)
    repeat(BAND_MOTIFS) { index ->
        rotate(index * BAND_STEP_DEGREES, pivot = center) {
            drawPath(ornament.bodom, color = if (index % 2 == 0) palette.petals else palette.center)
        }
        rotate(index * BAND_STEP_DEGREES + BAND_STEP_DEGREES / 2f, pivot = center) {
            drawCircle(bandDot, radius = 2f * unit, center = center + Offset(0f, -84f * unit))
        }
    }
    drawCircle(palette.petals, radius = 73f * unit, center = center, style = ornament.innerRingStroke)
}
