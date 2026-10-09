package tj.umar.navoplayer.core.domain.model

const val BASS_BOOST_MAX_STRENGTH = 1000
const val EQUALIZER_LEVEL_STEP_MB = 100

data class EqualizerBand(val index: Int, val centerFrequencyHz: Int)

data class EqualizerPreset(val index: Int, val name: String, val bandLevelsMb: List<Int>)

data class EqualizerCapabilities(
    val bands: List<EqualizerBand>,
    val minLevelMb: Int,
    val maxLevelMb: Int,
    val presets: List<EqualizerPreset>,
    val bassBoostSupported: Boolean,
)

sealed interface EqualizerAvailability {
    data object Probing : EqualizerAvailability
    data object Unsupported : EqualizerAvailability
    data class Supported(val capabilities: EqualizerCapabilities) : EqualizerAvailability
}

sealed interface EqualizerPresetSelection {
    data object Custom : EqualizerPresetSelection
    data class Preset(val index: Int) : EqualizerPresetSelection
}

data class EqualizerSettings(
    val enabled: Boolean = false,
    val preset: EqualizerPresetSelection = EqualizerPresetSelection.Custom,
    val customBandLevelsMb: List<Int> = emptyList(),
    val bassBoostStrength: Int = 0,
)

data class EqualizerProfile(
    val enabled: Boolean,
    val bandLevelsMb: List<Int>,
    val selectedPreset: EqualizerPreset?,
    val bassBoostStrength: Int,
)

sealed interface EqualizerStatus {
    data object Probing : EqualizerStatus
    data class Unsupported(val settings: EqualizerSettings) : EqualizerStatus
    data class Ready(
        val capabilities: EqualizerCapabilities,
        val settings: EqualizerSettings,
        val profile: EqualizerProfile,
    ) : EqualizerStatus
}
