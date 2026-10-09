package tj.umar.navoplayer.core.designsystem.medallion

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

class OrnamentTest {

    private val center = Offset(100f, 100f)

    @Test
    fun `eight point star has sixteen alternating vertices`() {
        val vertices = starVertices(points = 8, outerRadius = 60f, innerRadius = 40f, center = center)

        assertEquals(16, vertices.size)
        vertices.forEachIndexed { index, point ->
            val expected = if (index % 2 == 0) 60f else 40f
            assertEquals(expected, (point - center).getDistance(), 1e-3f)
        }
    }

    @Test
    fun `first star point faces up`() {
        val top = starVertices(points = 8, outerRadius = 60f, innerRadius = 40f, center = center).first()

        assertEquals(100f, top.x, 1e-3f)
        assertEquals(40f, top.y, 1e-3f)
    }

    @Test
    fun `khotam inner radius matches two overlapping squares`() {
        val expected = (60.0 * cos(PI / 4) / cos(PI / 8)).toFloat()

        assertEquals(expected, khotamInnerRadius(60f), 1e-4f)
    }

    @Test
    fun `rosette breath stays at rest without rotation and peaks within six percent`() {
        assertEquals(1f, breathScale(0f), 1e-6f)
        assertEquals(1.06f, breathScale(9f), 1e-4f)
        assertEquals(0.94f, breathScale(27f), 1e-4f)
    }
}
