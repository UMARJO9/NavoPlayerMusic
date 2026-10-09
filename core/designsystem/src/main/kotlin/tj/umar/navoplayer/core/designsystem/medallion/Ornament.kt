package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal fun starVertices(points: Int, outerRadius: Float, innerRadius: Float, center: Offset): List<Offset> {
    val step = PI / points
    return List(points * 2) { index ->
        val radius = if (index % 2 == 0) outerRadius else innerRadius
        val angle = index * step - PI / 2
        center + Offset((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
    }
}

internal fun khotamInnerRadius(outerRadius: Float): Float =
    outerRadius * (cos(PI / 4) / cos(PI / 8)).toFloat()

internal fun starPath(vertices: List<Offset>): Path = Path().apply {
    vertices.forEachIndexed { index, point ->
        if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
    }
    close()
}

internal fun tulipPath(base: Offset, length: Float, width: Float): Path = Path().apply {
    val x = base.x
    val y = base.y
    moveTo(x, y)
    cubicTo(x - width, y - length * 0.25f, x - width * 0.7f, y - length * 0.8f, x - width * 0.4f, y - length)
    lineTo(x - width * 0.15f, y - length * 0.8f)
    lineTo(x, y - length * 1.05f)
    lineTo(x + width * 0.15f, y - length * 0.8f)
    lineTo(x + width * 0.4f, y - length)
    cubicTo(x + width * 0.7f, y - length * 0.8f, x + width, y - length * 0.25f, x, y)
    close()
}

internal fun bodomPath(center: Offset, length: Float, width: Float): Path = Path().apply {
    val x = center.x
    val y = center.y
    val half = length / 2f
    moveTo(x + width * 0.35f, y - half)
    cubicTo(x + width, y - half * 0.2f, x + width * 0.8f, y + half, x, y + half)
    cubicTo(x - width * 0.8f, y + half, x - width, y - half * 0.1f, x - width * 0.1f, y - half * 0.55f)
    quadraticTo(x + width * 0.1f, y - half * 0.8f, x + width * 0.35f, y - half)
    close()
}
