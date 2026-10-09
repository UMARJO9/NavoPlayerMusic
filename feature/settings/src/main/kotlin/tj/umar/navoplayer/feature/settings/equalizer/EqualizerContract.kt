package tj.umar.navoplayer.feature.settings.equalizer

import androidx.compose.runtime.Immutable
import tj.umar.navoplayer.core.domain.model.EqualizerPreset
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection

internal enum class EqualizerPhase { Loading, Ready, Unsupported, LoadFailed }

@Immutable
internal data class EqualizerBandUi(val index: Int, val centerFrequencyHz: Int, val levelMb: Int)

@Immutable
internal data class EqualizerState(
    val phase: EqualizerPhase = EqualizerPhase.Loading,
    val enabled: Boolean = false,
    val bands: List<EqualizerBandUi> = emptyList(),
    val minLevelMb: Int = 0,
    val maxLevelMb: Int = 0,
    val presets: List<EqualizerPreset> = emptyList(),
    val selectedPreset: EqualizerPresetSelection = EqualizerPresetSelection.Custom,
    val bassBoostSupported: Boolean = false,
    val bassBoostStrength: Int = 0,
    val draggingBand: Int? = null,
    val draggingBassBoost: Boolean = false,
) {
    val controlsEnabled: Boolean
        get() = enabled && phase == EqualizerPhase.Ready
}

internal sealed interface EqualizerIntent {
    data object ScreenStarted : EqualizerIntent
    data object ScreenStopped : EqualizerIntent
    data object RetryLoad : EqualizerIntent
    data class EnabledToggled(val enabled: Boolean) : EqualizerIntent
    data class PresetSelected(val index: Int) : EqualizerIntent
    data object CustomSelected : EqualizerIntent
    data class BandLevelChanged(val band: Int, val levelMb: Int) : EqualizerIntent
    data class BandLevelChangeFinished(val band: Int) : EqualizerIntent
    data class BassBoostChanged(val strength: Int) : EqualizerIntent
    data object BassBoostChangeFinished : EqualizerIntent
    data object ResetClicked : EqualizerIntent
    data object BackClicked : EqualizerIntent
}

internal sealed interface EqualizerEffect {
    data object NavigateBack : EqualizerEffect
    data object ShowSaveFailed : EqualizerEffect
}
