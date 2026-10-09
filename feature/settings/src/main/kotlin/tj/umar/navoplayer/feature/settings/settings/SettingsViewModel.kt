package tj.umar.navoplayer.feature.settings.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.domain.model.EqualizerStatus
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.GetAppInfoUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveEqualizerUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import tj.umar.navoplayer.core.domain.usecase.SetMinTrackDurationUseCase
import tj.umar.navoplayer.core.domain.usecase.SetPauseOnHeadphonesDisconnectUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val observeSettings: ObserveSettingsUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val setMinTrackDuration: SetMinTrackDurationUseCase,
    private val setPauseOnHeadphonesDisconnect: SetPauseOnHeadphonesDisconnectUseCase,
    private val observeEqualizer: ObserveEqualizerUseCase,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState(versionName = getAppInfo().versionName)) {

    private var settingsJob: Job? = null
    private var equalizerJob: Job? = null

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.ScreenStarted,
            SettingsIntent.RetryLoad -> startObserving()
            SettingsIntent.ScreenStopped -> stopObserving()
            is SettingsIntent.MinTrackDurationSelected -> onDurationSelected(intent.value)
            is SettingsIntent.PauseOnHeadphonesDisconnectToggled ->
                if (!currentState.loadFailed) save { setPauseOnHeadphonesDisconnect(intent.enabled) }
            SettingsIntent.HiddenFoldersClicked -> sendEffect(SettingsEffect.NavigateToHiddenFolders)
            SettingsIntent.LicensesClicked -> sendEffect(SettingsEffect.NavigateToLicenses)
            SettingsIntent.EqualizerClicked -> sendEffect(SettingsEffect.NavigateToEqualizer)
            SettingsIntent.BackClicked -> sendEffect(SettingsEffect.NavigateBack)
        }
    }

    private fun onSettings(settings: UserSettings) {
        setState {
            copy(
                isLoading = false,
                loadFailed = false,
                minTrackDuration = settings.minTrackDuration,
                hiddenFolderCount = settings.excludedFolders.size,
                pauseOnHeadphonesDisconnect = settings.pauseOnHeadphonesDisconnect,
            )
        }
    }

    private fun onDurationSelected(value: MinTrackDuration) {
        if (currentState.loadFailed) return
        if (value == currentState.minTrackDuration) return
        save { setMinTrackDuration(value) }
    }

    private fun save(write: suspend () -> NavoResult<Unit>) {
        viewModelScope.launch {
            write().onError { sendEffect(SettingsEffect.ShowSaveFailed) }
        }
    }

    private fun startObserving() {
        startObservingEqualizer()
        if (settingsJob?.isActive == true) return
        settingsJob = observeSettings()
            .onEach(::onSettings)
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun startObservingEqualizer() {
        if (equalizerJob?.isActive == true) return
        equalizerJob = observeEqualizer()
            .onEach { status -> setState { copy(equalizerSummary = status.toSummary()) } }
            .catch { setState { copy(equalizerSummary = EqualizerSummary.Off) } }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        equalizerJob?.cancel()
        equalizerJob = null
        settingsJob?.cancel()
        settingsJob = null
    }
}

private fun EqualizerStatus.toSummary(): EqualizerSummary = when (this) {
    EqualizerStatus.Probing -> EqualizerSummary.Loading
    is EqualizerStatus.Unsupported -> EqualizerSummary.Unsupported
    is EqualizerStatus.Ready -> when {
        !profile.enabled -> EqualizerSummary.Off
        else -> profile.selectedPreset?.let { EqualizerSummary.Preset(it.name) } ?: EqualizerSummary.Custom
    }
}
