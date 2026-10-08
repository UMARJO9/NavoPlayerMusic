package tj.umar.navoplayer.feature.settings.folders

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tj.umar.navoplayer.core.common.result.onError
import tj.umar.navoplayer.core.domain.usecase.ObserveAllFoldersUseCase
import tj.umar.navoplayer.core.domain.usecase.SetFolderExcludedUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class HiddenFoldersViewModel @Inject constructor(
    private val observeAllFolders: ObserveAllFoldersUseCase,
    private val setFolderExcluded: SetFolderExcludedUseCase,
) : MviViewModel<HiddenFoldersState, HiddenFoldersIntent, HiddenFoldersEffect>(HiddenFoldersState()) {

    private var foldersJob: Job? = null
    private var hasLoaded = false

    override fun onIntent(intent: HiddenFoldersIntent) {
        when (intent) {
            is HiddenFoldersIntent.ScreenStarted -> onScreenStarted(intent.hasPermission)
            HiddenFoldersIntent.ScreenStopped -> stopObserving()
            HiddenFoldersIntent.RetryLoad -> startObserving()
            is HiddenFoldersIntent.FolderVisibilityToggled -> onVisibilityToggled(intent.path, intent.visible)
            HiddenFoldersIntent.BackClicked -> sendEffect(HiddenFoldersEffect.NavigateBack)
        }
    }

    private fun onScreenStarted(hasPermission: Boolean) {
        if (!hasPermission) {
            sendEffect(HiddenFoldersEffect.NavigateToWelcome)
            return
        }
        startObserving()
    }

    private fun onVisibilityToggled(path: String, visible: Boolean) {
        viewModelScope.launch {
            setFolderExcluded(path, !visible).onError { sendEffect(HiddenFoldersEffect.ShowSaveFailed) }
        }
    }

    private fun startObserving() {
        if (foldersJob?.isActive == true) return
        setState { copy(isLoading = !hasLoaded, loadFailed = false) }
        foldersJob = observeAllFolders()
            .onEach { folders ->
                hasLoaded = true
                setState { copy(isLoading = false, folders = folders) }
            }
            .catch { setState { copy(isLoading = false, loadFailed = true) } }
            .launchIn(viewModelScope)
    }

    private fun stopObserving() {
        foldersJob?.cancel()
        foldersJob = null
    }
}
