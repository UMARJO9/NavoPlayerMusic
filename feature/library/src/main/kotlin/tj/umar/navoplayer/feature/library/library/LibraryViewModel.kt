package tj.umar.navoplayer.feature.library.library

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tj.umar.navoplayer.core.domain.usecase.ObserveTracksUseCase
import tj.umar.navoplayer.core.ui.mvi.MviViewModel
import javax.inject.Inject

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val observeTracks: ObserveTracksUseCase,
) : MviViewModel<LibraryState, LibraryIntent, LibraryEffect>(LibraryState()) {

    private var tracksJob: Job? = null

    private var deniedWithoutRationale = false

    override fun onIntent(intent: LibraryIntent) {
        when (intent) {
            is LibraryIntent.TabSelected -> selectTab(intent.tab)
            is LibraryIntent.PermissionChecked -> onPermissionChecked(intent.granted)
            is LibraryIntent.PermissionResult -> onPermissionResult(intent)
            LibraryIntent.GrantPermissionClicked -> onGrantPermissionClicked()
        }
    }

    private fun selectTab(tab: LibraryTab) {
        if (tab == currentState.selectedTab) return
        setState { copy(selectedTab = tab) }
    }

    private fun onPermissionChecked(granted: Boolean) {
        if (granted) {
            onPermissionGranted()
            return
        }
        when (currentState.audioPermission) {
            AudioPermissionStatus.Unknown -> {
                setState { copy(audioPermission = AudioPermissionStatus.Denied) }
                sendEffect(LibraryEffect.RequestAudioPermission)
            }
            AudioPermissionStatus.PermanentlyDenied -> Unit
            else -> setState { copy(audioPermission = AudioPermissionStatus.Denied) }
        }
    }

    private fun onPermissionResult(result: LibraryIntent.PermissionResult) {
        when {
            result.granted -> {
                deniedWithoutRationale = false
                onPermissionGranted()
            }
            result.rationaleAfter -> {
                deniedWithoutRationale = false
                setState { copy(audioPermission = AudioPermissionStatus.Denied) }
            }
            result.rationaleBefore || deniedWithoutRationale ->
                setState { copy(audioPermission = AudioPermissionStatus.PermanentlyDenied) }
            else -> {
                deniedWithoutRationale = true
                setState { copy(audioPermission = AudioPermissionStatus.Denied) }
            }
        }
    }

    private fun onGrantPermissionClicked() {
        val effect = if (currentState.audioPermission == AudioPermissionStatus.PermanentlyDenied) {
            LibraryEffect.OpenAppSettings
        } else {
            LibraryEffect.RequestAudioPermission
        }
        sendEffect(effect)
    }

    private fun onPermissionGranted() {
        if (currentState.audioPermission != AudioPermissionStatus.Granted) {
            setState { copy(audioPermission = AudioPermissionStatus.Granted) }
        }
        startObservingTracks()
    }

    private fun startObservingTracks() {
        if (tracksJob?.isActive == true) return
        setState { copy(isLoadingTracks = true, tracksLoadFailed = false) }
        tracksJob = observeTracks()
            .onEach { tracks -> setState { copy(tracks = tracks, isLoadingTracks = false) } }
            .catch { setState { copy(isLoadingTracks = false, tracksLoadFailed = true) } }
            .launchIn(viewModelScope)
    }
}
