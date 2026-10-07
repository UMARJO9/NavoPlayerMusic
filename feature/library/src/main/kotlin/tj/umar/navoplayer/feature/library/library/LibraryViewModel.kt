package tj.umar.navoplayer.feature.library.library

import androidx.lifecycle.SavedStateHandle
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
    private val savedStateHandle: SavedStateHandle,
) : MviViewModel<LibraryState, LibraryIntent, LibraryEffect>(
    LibraryState(audioPermission = savedStateHandle.restoredPermission()),
) {

    private var tracksJob: Job? = null

    private var deniedWithoutRationale: Boolean
        get() = savedStateHandle[KEY_DENIED_WITHOUT_RATIONALE] ?: false
        set(value) {
            savedStateHandle[KEY_DENIED_WITHOUT_RATIONALE] = value
        }

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
                updatePermission(AudioPermissionStatus.Denied)
                sendEffect(LibraryEffect.RequestAudioPermission)
            }
            AudioPermissionStatus.PermanentlyDenied -> Unit
            else -> updatePermission(AudioPermissionStatus.Denied)
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
                updatePermission(AudioPermissionStatus.Denied)
            }
            result.rationaleBefore || deniedWithoutRationale ->
                updatePermission(AudioPermissionStatus.PermanentlyDenied)
            else -> {
                deniedWithoutRationale = true
                updatePermission(AudioPermissionStatus.Denied)
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
            updatePermission(AudioPermissionStatus.Granted)
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

    private fun updatePermission(status: AudioPermissionStatus) {
        savedStateHandle[KEY_AUDIO_PERMISSION] = status.name
        setState { copy(audioPermission = status) }
    }
}

private const val KEY_AUDIO_PERMISSION = "audio_permission"
private const val KEY_DENIED_WITHOUT_RATIONALE = "denied_without_rationale"

private fun SavedStateHandle.restoredPermission(): AudioPermissionStatus =
    get<String>(KEY_AUDIO_PERMISSION)
        ?.let { name -> AudioPermissionStatus.entries.firstOrNull { it.name == name } }
        ?: AudioPermissionStatus.Unknown
