package tj.umar.navoplayer.feature.settings.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.NavoResult
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.domain.model.MinTrackDuration
import tj.umar.navoplayer.core.domain.model.UserSettings
import tj.umar.navoplayer.core.domain.usecase.GetAppInfoUseCase
import tj.umar.navoplayer.core.domain.usecase.ObserveSettingsUseCase
import tj.umar.navoplayer.core.domain.usecase.SetMinTrackDurationUseCase
import tj.umar.navoplayer.core.domain.usecase.SetPauseOnHeadphonesDisconnectUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    getAppInfo: GetAppInfoUseCase,
    private val setMinTrackDuration: SetMinTrackDurationUseCase,
    private val setPauseOnHeadphonesDisconnect: SetPauseOnHeadphonesDisconnectUseCase,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(SettingsState(versionName = getAppInfo().versionName)) {

    init {
        observeSettings()
            .onEach(::onSettings)
            .catch { onSettings(UserSettings()) }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.MinTrackDurationSelected -> onDurationSelected(intent.value)
            is SettingsIntent.PauseOnHeadphonesDisconnectToggled -> save { setPauseOnHeadphonesDisconnect(intent.enabled) }
            SettingsIntent.HiddenFoldersClicked -> sendEffect(SettingsEffect.NavigateToHiddenFolders)
            SettingsIntent.LicensesClicked -> sendEffect(SettingsEffect.NavigateToLicenses)
            SettingsIntent.BackClicked -> sendEffect(SettingsEffect.NavigateBack)
        }
    }

    private fun onSettings(settings: UserSettings) {
        setState {
            copy(
                isLoading = false,
                minTrackDuration = settings.minTrackDuration,
                hiddenFolderCount = settings.excludedFolders.size,
                pauseOnHeadphonesDisconnect = settings.pauseOnHeadphonesDisconnect,
            )
        }
    }

    private fun onDurationSelected(value: MinTrackDuration) {
        if (value == currentState.minTrackDuration) return
        save { setMinTrackDuration(value) }
    }

    private fun save(write: suspend () -> NavoResult<Unit>) {
        viewModelScope.launch {
            write().onError { sendEffect(SettingsEffect.ShowSaveFailed) }
        }
    }
}
