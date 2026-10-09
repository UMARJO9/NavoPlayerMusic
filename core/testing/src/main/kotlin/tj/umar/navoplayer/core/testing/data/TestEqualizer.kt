package tj.umar.navoplayer.core.testing.data

import tj.umar.navoplayer.core.domain.model.EqualizerBand
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import tj.umar.navoplayer.core.domain.model.EqualizerPreset

val testEqualizerCapabilities = EqualizerCapabilities(
    bands = listOf(60, 230, 910, 3_600, 14_000).mapIndexed { index, hz -> EqualizerBand(index, hz) },
    minLevelMb = -1500,
    maxLevelMb = 1500,
    presets = listOf(
        EqualizerPreset(0, "Normal", listOf(300, 0, 0, 0, 300)),
        EqualizerPreset(1, "Classical", listOf(500, 300, -200, 400, 400)),
        EqualizerPreset(2, "Rock", listOf(500, 300, -100, 300, 500)),
    ),
    bassBoostSupported = true,
)

val testEqualizerCapabilitiesWithoutBass = testEqualizerCapabilities.copy(bassBoostSupported = false)
