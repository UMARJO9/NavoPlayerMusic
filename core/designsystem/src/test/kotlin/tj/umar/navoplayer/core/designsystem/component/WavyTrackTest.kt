package tj.umar.navoplayer.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI

class WavyTrackTest {

    private val wavelength = 40f
    private val amplitude = 6f

    @Test
    fun `wave starts at center line`() {
        assertEquals(0f, waveOffset(0f, wavelength, amplitude, phase = 0f), 1e-4f)
    }

    @Test
    fun `quarter wavelength reaches full amplitude`() {
        assertEquals(amplitude, waveOffset(wavelength / 4f, wavelength, amplitude, phase = 0f), 1e-4f)
        assertEquals(-amplitude, waveOffset(wavelength * 3f / 4f, wavelength, amplitude, phase = 0f), 1e-4f)
    }

    @Test
    fun `wave repeats every wavelength`() {
        val x = 7f

        assertEquals(
            waveOffset(x, wavelength, amplitude, phase = 0f),
            waveOffset(x + wavelength, wavelength, amplitude, phase = 0f),
            1e-4f,
        )
    }

    @Test
    fun `growing phase moves wave forward`() {
        val quarterTurn = (PI / 2).toFloat()

        assertEquals(
            waveOffset(10f, wavelength, amplitude, phase = 0f),
            waveOffset(20f, wavelength, amplitude, phase = quarterTurn),
            1e-4f,
        )
    }

    @Test
    fun `flat wave stays on center line`() {
        assertEquals(0f, waveOffset(13f, wavelength, amplitude = 0f, phase = 1f), 0f)
    }
}
