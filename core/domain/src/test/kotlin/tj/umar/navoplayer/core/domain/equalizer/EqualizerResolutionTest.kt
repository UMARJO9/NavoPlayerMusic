package tj.umar.navoplayer.core.domain.equalizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import tj.umar.navoplayer.core.domain.model.EqualizerBand
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import tj.umar.navoplayer.core.domain.model.EqualizerPreset
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerSettings

class EqualizerResolutionTest {

    private val rock = EqualizerPreset(index = 1, name = "Rock", bandLevelsMb = listOf(500, 300, -100))
    private val capabilities = EqualizerCapabilities(
        bands = listOf(EqualizerBand(0, 60), EqualizerBand(1, 910), EqualizerBand(2, 14_000)),
        minLevelMb = -1500,
        maxLevelMb = 1500,
        presets = listOf(EqualizerPreset(0, "Normal", listOf(0, 0, 0)), rock),
        bassBoostSupported = true,
    )

    @Test
    fun `valid preset uses preset levels`() {
        val profile = EqualizerSettings(enabled = true, preset = EqualizerPresetSelection.Preset(1)).resolve(capabilities)

        assertEquals(listOf(500, 300, -100), profile.bandLevelsMb)
        assertEquals(rock, profile.selectedPreset)
    }

    @Test
    fun `unknown preset falls back to custom levels`() {
        val profile = EqualizerSettings(
            preset = EqualizerPresetSelection.Preset(9),
            customBandLevelsMb = listOf(100, 200, 300),
        ).resolve(capabilities)

        assertNull(profile.selectedPreset)
        assertEquals(listOf(100, 200, 300), profile.bandLevelsMb)
    }

    @Test
    fun `custom levels are padded truncated clamped and snapped`() {
        assertEquals(listOf(200, 0, 0), EqualizerSettings(customBandLevelsMb = listOf(240)).resolve(capabilities).bandLevelsMb)
        assertEquals(
            listOf(1500, -1500, 100),
            EqualizerSettings(customBandLevelsMb = listOf(4000, -4000, 60, 700)).resolve(capabilities).bandLevelsMb,
        )
        assertEquals(listOf(0, 0, 0), EqualizerSettings().resolve(capabilities).bandLevelsMb)
    }

    @Test
    fun `bass boost is clamped or dropped when unsupported`() {
        assertEquals(1000, EqualizerSettings(bassBoostStrength = 5000).resolve(capabilities).bassBoostStrength)
        assertEquals(
            0,
            EqualizerSettings(bassBoostStrength = 500).resolve(capabilities.copy(bassBoostSupported = false)).bassBoostStrength,
        )
    }

    @Test
    fun `levels snap from range start and stay in range`() {
        val odd = capabilities.copy(minLevelMb = -1250, maxLevelMb = 1250)

        assertEquals(-1250, odd.snapLevel(-1300))
        assertEquals(1250, odd.snapLevel(1300))
        assertEquals(-150, odd.snapLevel(-140))
    }
}
