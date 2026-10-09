package tj.umar.navoplayer.core.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin

private const val FULL_TURN = (2 * PI).toFloat()
private const val FLAT_AMPLITUDE_PX = 0.5f
private const val WAVE_STEP_PX = 2f

internal fun waveOffset(x: Float, wavelength: Float, amplitude: Float, phase: Float): Float =
    amplitude * sin(FULL_TURN * x / wavelength - phase)

internal fun DrawScope.drawWavyTrack(
    fraction: Float,
    amplitude: Float,
    phase: Float,
    wavelength: Float,
    strokeWidth: Float,
    activeColor: Color,
    inactiveColor: Color,
) {
    val centerY = size.height / 2f
    val activeEnd = size.width * fraction.coerceIn(0f, 1f)
    drawLine(
        color = inactiveColor,
        start = Offset(activeEnd, centerY),
        end = Offset(size.width, centerY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    if (activeEnd <= 0f) return
    if (amplitude < FLAT_AMPLITUDE_PX) {
        drawLine(
            color = activeColor,
            start = Offset(0f, centerY),
            end = Offset(activeEnd, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        return
    }
    val path = Path().apply {
        moveTo(0f, centerY + waveOffset(0f, wavelength, amplitude, phase))
        var x = WAVE_STEP_PX
        while (x < activeEnd) {
            lineTo(x, centerY + waveOffset(x, wavelength, amplitude, phase))
            x += WAVE_STEP_PX
        }
        lineTo(activeEnd, centerY + waveOffset(activeEnd, wavelength, amplitude, phase))
    }
    drawPath(
        path = path,
        color = activeColor,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}
