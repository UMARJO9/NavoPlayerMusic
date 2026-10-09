package tj.umar.navoplayer.core.domain.equalizer

import tj.umar.navoplayer.core.domain.model.BASS_BOOST_MAX_STRENGTH
import tj.umar.navoplayer.core.domain.model.EQUALIZER_LEVEL_STEP_MB
import tj.umar.navoplayer.core.domain.model.EqualizerCapabilities
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerProfile
import tj.umar.navoplayer.core.domain.model.EqualizerSettings
import kotlin.math.roundToInt

fun EqualizerSettings.resolve(capabilities: EqualizerCapabilities): EqualizerProfile {
    val selection = preset
    val presetMatch = if (selection is EqualizerPresetSelection.Preset) {
        capabilities.presets.firstOrNull { it.index == selection.index }
    } else {
        null
    }
    val levels = capabilities.normalizeLevels(presetMatch?.bandLevelsMb ?: customBandLevelsMb)
    return EqualizerProfile(
        enabled = enabled,
        bandLevelsMb = levels,
        selectedPreset = presetMatch,
        bassBoostStrength = if (capabilities.bassBoostSupported) {
            bassBoostStrength.coerceIn(0, BASS_BOOST_MAX_STRENGTH)
        } else {
            0
        },
    )
}

fun EqualizerCapabilities.normalizeLevels(levels: List<Int>): List<Int> =
    bands.indices.map { index -> snapLevel(levels.getOrElse(index) { 0 }) }

fun EqualizerCapabilities.snapLevel(levelMb: Int): Int {
    val snapped = (levelMb.toFloat() / EQUALIZER_LEVEL_STEP_MB).roundToInt() * EQUALIZER_LEVEL_STEP_MB
    return snapped.coerceIn(minLevelMb, maxLevelMb)
}
