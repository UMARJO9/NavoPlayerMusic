package tj.umar.navoplayer.feature.settings.equalizer

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.domain.model.EqualizerPresetSelection
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.domain.usecase.ObserveEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.ResetEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.SelectEqualizerPresetUseCase
import tj.umar.navoplayer.core.domain.usecase.SetBassBoostStrengthUseCase
import tj.umar.navoplayer.core.domain.usecase.SetEqualizerBandLevelUseCase
import tj.umar.navoplayer.core.domain.usecase.SetEqualizerEnabledUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

private const val BASS_BOOST_DRAFT_KEY = "bass_boost"

@HiltViewModel
internal class EqualizerViewModel @Inject constructor(
    private val observeEqualizer: ObserveEqualizerUseCase,
    private val setEqualizerEnabled: SetEqualizerEnabledUseCase,
    private val selectEqualizerPreset: SelectEqualizerPresetUseCase,
    private val setEqualizerBandLevel: SetEqualizerBandLevelUseCase,
    private val setBassBoostStrength: SetBassBoostStrengthUseCase,
    private val resetEqualizer: ResetEqualizerUseCase,
) : MviViewModel<EqualizerState, EqualizerIntent, EqualizerEffect>(EqualizerState()) {

    private var observeJob: Job? = null
    private val writeLock = Mutex()
    private val pendingDrafts = mutableMapOf<Any, suspend () -> NavoResult<Unit>>()

    override fun onIntent(intent: EqualizerIntent) {
        when (intent) {
            EqualizerIntent.ScreenStarted,
            EqualizerIntent.RetryLoad -> startObserving()
            EqualizerIntent.ScreenStopped -> stopObserving()
            is EqualizerIntent.EnabledToggled -> onEnabledToggled(intent.enabled)
            is EqualizerIntent.PresetSelected ->
                ifControlsEnabled { save { selectEqualizerPreset(EqualizerPresetSelection.Preset(intent.index)) } }
            EqualizerIntent.CustomSelected ->
                ifControlsEnabled { save { selectEqualizerPreset(EqualizerPresetSelection.Custom) } }
            is EqualizerIntent.BandLevelChanged -> onBandLevelChanged(intent.band, intent.levelMb)
            is EqualizerIntent.BandLevelChangeFinished -> setState { copy(draggingBand = null) }
            is EqualizerIntent.BassBoostChanged -> onBassBoostChanged(intent.strength)
            EqualizerIntent.BassBoostChangeFinished -> setState { copy(draggingBassBoost = false) }
            EqualizerIntent.ResetClicked -> ifControlsEnabled { save { resetEqualizer() } }
            EqualizerIntent.BackClicked -> sendEffect(EqualizerEffect.NavigateBack)
        }
    }

    private fun onEnabledToggled(enabled: Boolean) {
        if (currentState.phase != EqualizerPhase.Ready) return
        setState { copy(enabled = enabled) }
        save { setEqualizerEnabled(enabled) }
    }

    private fun onBandLevelChanged(band: Int, levelMb: Int) = ifControlsEnabled {
        if (currentState.bands.none { it.index == band }) return@ifControlsEnabled
        setState {
            copy(
                bands = bands.map { if (it.index == band) it.copy(levelMb = levelMb) else it },
                draggingBand = band,
                selectedPreset = EqualizerPresetSelection.Custom,
            )
        }
        writeDraft(band) { setEqualizerBandLevel(band, levelMb) }
    }

    private fun onBassBoostChanged(strength: Int) = ifControlsEnabled {
        if (!currentState.bassBoostSupported) return@ifControlsEnabled
        setState { copy(bassBoostStrength = strength, draggingBassBoost = true) }
        writeDraft(BASS_BOOST_DRAFT_KEY) { setBassBoostStrength(strength) }
    }

    private fun onStatus(status: EqualizerStatus) {
        when (status) {
            EqualizerStatus.Probing -> setState { copy(phase = EqualizerPhase.Loading) }
            is EqualizerStatus.Unsupported -> setState { copy(phase = EqualizerPhase.Unsupported) }
            is EqualizerStatus.Ready -> setState {
                val levels = status.profile.bandLevelsMb
                copy(
                    phase = EqualizerPhase.Ready,
                    enabled = status.profile.enabled,
                    bands = status.capabilities.bands.map { band ->
                        val draft = bands.firstOrNull { it.index == band.index }?.levelMb
                        val level = if (band.index == draggingBand && draft != null) draft else levels[band.index]
                        EqualizerBandUi(band.index, band.centerFrequencyHz, level)
                    },
                    minLevelMb = status.capabilities.minLevelMb,
                    maxLevelMb = status.capabilities.maxLevelMb,
                    presets = status.capabilities.presets,
                    selectedPreset = if (draggingBand != null) {
                        EqualizerPresetSelection.Custom
                    } else {
                        status.profile.selectedPreset?.let { EqualizerPresetSelection.Preset(it.index) }
                            ?: EqualizerPresetSelection.Custom
                    },
                    bassBoostSupported = status.capabilities.bassBoostSupported,
                    bassBoostStrength = if (draggingBassBoost) bassBoostStrength else status.profile.bassBoostStrength,
                )
            }
        }
    }

    private inline fun ifControlsEnabled(action: () -> Unit) {
        if (currentState.controlsEnabled) action()
    }

    private fun writeDraft(key: Any, write: suspend () -> NavoResult<Unit>) {
        pendingDrafts[key] = write
        viewModelScope.launch {
            writeLock.withLock {
                val pending = pendingDrafts.remove(key) ?: return@withLock
                report(pending())
            }
        }
    }

    private fun save(write: suspend () -> NavoResult<Unit>) {
        viewModelScope.launch { writeLock.withLock { report(write()) } }
    }

    private fun report(result: NavoResult<Unit>) {
        result.onError { sendEffect(EqualizerEffect.ShowSaveFailed) }
    }

    private fun startObserving() {
        if (observeJob?.isActive == true) return
        if (currentState.phase == EqualizerPhase.LoadFailed) setState { copy(phase = EqualizerPhase.Loading) }
        observeJob = observeEqualizer()
            .onEach(::onStatus)
            .catch { setState { copy(phase = EqualizerPhase.LoadFailed) } }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        observeJob?.cancel()
        observeJob = null
        setState { copy(draggingBand = null, draggingBassBoost = false) }
    }
}
